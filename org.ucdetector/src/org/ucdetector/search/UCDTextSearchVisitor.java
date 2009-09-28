/**
 * Copyright (c) 2009 Joerg Spieler
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.ucdetector.search;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.search.core.text.TextSearchMatchAccess;
import org.eclipse.search.core.text.TextSearchRequestor;
import org.eclipse.search.core.text.TextSearchScope;
import org.eclipse.search.internal.core.text.FileCharSequenceProvider;
import org.eclipse.search.internal.ui.Messages;
import org.eclipse.search.internal.ui.SearchMessages;
import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.ui.NewSearchUI;
import org.ucdetector.Log;

/**
 * This class is a copy of {@link org.eclipse.search.internal.core.text.TextSearchVisitor}
 * but the following UI code was removed, to run it in headless mode:
 * <ul>
 *   <li><code>import org.eclipse.jface.*</code></li>
 *   <li><code>import org.eclipse.ui.*</code></li>
 * </ul>
 */
class UCDTextSearchVisitor {
  private final TextSearchRequestor fCollector;
  private final Matcher fMatcher;

  private IProgressMonitor fProgressMonitor;

  private int fNumberOfScannedFiles;
  private int fNumberOfFilesToScan;
  private IFile fCurrentFile;

  private final MultiStatus fStatus;

  private final FileCharSequenceProvider fFileCharSequenceProvider;

  private final TextSearchMatchAccessImpl fMatchAccess;

  protected UCDTextSearchVisitor(TextSearchRequestor collector,
      Pattern searchPattern) {
    fCollector = collector;
    fStatus = new MultiStatus(NewSearchUI.PLUGIN_ID, IStatus.OK,
        SearchMessages.TextSearchEngine_statusMessage, null);

    fMatcher = searchPattern.pattern().length() == 0 ? null : searchPattern
        .matcher(""); //$NON-NLS-1$

    fFileCharSequenceProvider = new FileCharSequenceProvider();
    fMatchAccess = new TextSearchMatchAccessImpl();
  }

  private IStatus search(IFile[] files, IProgressMonitor monitor) {
    fProgressMonitor = monitor == null ? new NullProgressMonitor() : monitor;
    fNumberOfScannedFiles = 0;
    fNumberOfFilesToScan = files.length;
    fCurrentFile = null;

    Job monitorUpdateJob = new Job(
        SearchMessages.TextSearchVisitor_progress_updating_job) {
      private int fLastNumberOfScannedFiles = 0;

      @Override
      public IStatus run(IProgressMonitor inner) {
        while (!inner.isCanceled()) {
          IFile file = fCurrentFile;
          if (file != null) {
            String fileName = file.getName();
            Object[] args = { fileName, Integer.valueOf(fNumberOfScannedFiles),
                Integer.valueOf(fNumberOfFilesToScan) };
            fProgressMonitor.subTask(Messages.format(
                SearchMessages.TextSearchVisitor_scanning, args));
            int steps = fNumberOfScannedFiles - fLastNumberOfScannedFiles;
            fProgressMonitor.worked(steps);
            fLastNumberOfScannedFiles += steps;
          }
          try {
            Thread.sleep(100);
          }
          catch (InterruptedException e) {
            return Status.OK_STATUS;
          }
        }
        return Status.OK_STATUS;
      }
    };

    try {
      String taskName = fMatcher == null ? SearchMessages.TextSearchVisitor_filesearch_task_label
          : Messages.format(
              SearchMessages.TextSearchVisitor_textsearch_task_label, fMatcher
                  .pattern().pattern());
      fProgressMonitor.beginTask(taskName, fNumberOfFilesToScan);
      monitorUpdateJob.setSystem(true);
      monitorUpdateJob.schedule();
      try {
        fCollector.beginReporting();
        processFiles(files);
        return fStatus;
      }
      finally {
        monitorUpdateJob.cancel();
      }
    }
    finally {
      fProgressMonitor.done();
      fCollector.endReporting();
    }
  }

  protected IStatus search(TextSearchScope scope, IProgressMonitor monitor) {
    return search(scope.evaluateFilesInScope(fStatus), monitor);
  }

  private void processFiles(IFile[] files) {
    for (IFile file : files) {
      fCurrentFile = file;
      boolean res = processFile(fCurrentFile);
      if (!res) {
        break;
      }
    }
  }

  private boolean processFile(IFile file) {
    try {
      if (!fCollector.acceptFile(file) || fMatcher == null) {
        return true;
      }
      CharSequence seq = null;
      try {
        seq = fFileCharSequenceProvider.newCharSequence(file);
        if (hasBinaryContent(seq, file) && !fCollector.reportBinaryFile(file)) {
          return true;
        }
        locateMatches(file, seq);
      }
      catch (FileCharSequenceProvider.FileCharSequenceException e) {
        e.throwWrappedException();
      }
      finally {
        if (seq != null) {
          try {
            fFileCharSequenceProvider.releaseCharSequence(seq);
          }
          catch (IOException e) {
            SearchPlugin.log(e);
          }
        }
      }
      //      }
    }
    catch (Throwable e) {
      fStatus.add(new Status(IStatus.ERROR, NewSearchUI.PLUGIN_ID,
          IStatus.ERROR, e.getMessage(), e));
    }
    finally {
      fNumberOfScannedFiles++;
    }
    if (fProgressMonitor.isCanceled()) {
      throw new OperationCanceledException(
          SearchMessages.TextSearchVisitor_canceled);
    }
    return true;
  }

  private boolean hasBinaryContent(CharSequence seq, IFile file)
      throws CoreException {
    IContentDescription desc = file.getContentDescription();
    if (desc != null) {
      IContentType contentType = desc.getContentType();
      if (contentType != null
          && contentType.isKindOf(Platform.getContentTypeManager()
              .getContentType(IContentTypeManager.CT_TEXT))) {
        return false;
      }
    }

    // avoid calling seq.length() at it runs through the complete file,
    // thus it would do so for all binary files.
    try {
      int limit = FileCharSequenceProvider.BUFFER_SIZE;
      for (int i = 0; i < limit; i++) {
        if (seq.charAt(i) == '\0') {
          return true;
        }
      }
    }
    catch (IndexOutOfBoundsException e) {
      Log.logError("Problems in hasBinaryContent: ", e); //$NON-NLS-1$
    }
    return false;
  }

  private void locateMatches(IFile file, CharSequence searchInput)
      throws CoreException {
    try {
      fMatcher.reset(searchInput);
      int k = 0;
      while (fMatcher.find()) {
        int start = fMatcher.start();
        int end = fMatcher.end();
        if (end != start) { // don't report 0-length matches
          fMatchAccess.initialize(file, start, end - start, searchInput);
          boolean res = fCollector.acceptPatternMatch(fMatchAccess);
          if (!res) {
            return; // no further reporting requested
          }
        }
        if (k++ == 20) {
          if (fProgressMonitor.isCanceled()) {
            throw new OperationCanceledException(
                SearchMessages.TextSearchVisitor_canceled);
          }
          k = 0;
        }
      }
    }
    finally {
      fMatchAccess.initialize(null, 0, 0, ""); // clear references //$NON-NLS-1$
    }
  }

  private static class TextSearchMatchAccessImpl extends TextSearchMatchAccess {

    private int fOffset;
    private int fLength;
    private IFile fFile;
    private CharSequence fContent;

    private void initialize(IFile file, int offset, int length,
        CharSequence content) {
      fFile = file;
      fOffset = offset;
      fLength = length;
      fContent = content;
    }

    @Override
    public IFile getFile() {
      return fFile;
    }

    @Override
    public int getMatchOffset() {
      return fOffset;
    }

    @Override
    public int getMatchLength() {
      return fLength;
    }

    @Override
    public int getFileContentLength() {
      return fContent.length();
    }

    @Override
    public char getFileContentChar(int offset) {
      return fContent.charAt(offset);
    }

    @Override
    public String getFileContent(int offset, int length) {
      return fContent.subSequence(offset, offset + length).toString(); // must pass a copy!
    }
  }
}
