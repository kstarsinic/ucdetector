package org.ucdetector.example.visibility;

public class UseProtectedClass // Marker YES
{
	// USE
	public final String useProtected_public = "useProtected";// Marker YES

	public static final String USE_PROTECTED = "useProtected";// Marker YES

	public void useProtected() // Marker YES
	{
	}

	public static void useProtectedStatic() // Marker YES
	{
	}

	// DONT
	private final String useProtected_private = "1";

	final String useProtected_default = "1";

	protected final String useProtected_protected = "1";

	/** we ingore default constructors! */
	public UseProtectedClass()
	{
	}

	public void dontUseProtected()
	{
	}

	public static void dontUseProtectedStatic()
	{
	}

	public static void main(String[] args)
	{
		UseProtectedClass useProtectedClass = new UseProtectedClass();
		System.out.println(useProtectedClass.useProtected_private);
	}
}
