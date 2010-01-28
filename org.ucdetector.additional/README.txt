How to add a new custom detector

- Action
  - Add a new Action, e.g. CheckUcdMarkerAction
  - Add plugin.xml#org.eclipse.ui.popupMenus like CheckUcdMarkerAction

- Add a new Iterator e.g. CheckNameConventionIterator
  - Add constant like org.ucdetector.iterator.CheckUcdMarkerIterator.ANALYZE_MARKER_CHECK_UCD_MARKERS
  - Call AdditionalIterator.createMarker() 
  - Or create marker like: org.ucdetector.iterator.CheckUcdMarkerIterator.createMarker()

- Marker
  - Add plugin.xml # org.eclipse.core.resources.markers                   like org.ucdetector.analyzeMarkerCheckUcdMarkers
  - Add plugin.xml # org.eclipse.ui.editors.annotationTypes               like org.ucdetector.analyzeMarkerCheckUcdMarkers
  - Add plugin.xml # org.eclipse.ui.editors.markerAnnotationSpecification like org.ucdetector.analyzeMarkerCheckUcdMarkers
  
- L18N
  - Add plugin.properties # like check.ucd.marker.name
  - Add plugin.properties # like check.ucd.specification.label