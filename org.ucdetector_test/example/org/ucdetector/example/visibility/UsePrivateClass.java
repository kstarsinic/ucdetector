package org.ucdetector.example.visibility;

// -------------------------------------------------------------------------
// UCE PRIVATE
// -------------------------------------------------------------------------
public class UsePrivateClass
{
	// USE
	final String usePrivate_default = "1";// Marker YES

	protected final String usePrivate_protected = "2";// Marker YES

	public final String usePrivate_public = "3";// Marker YES

	public static final String USE_PRIVATE = "4";// Marker YES

	public void usePrivate() // Marker YES
	{
	}

	public static void usePrivateStatic() // Marker YES
	{
	}

	// DONT
	private final String dontUsePrivate_private = "1";

	final String dontUsePrivate_default = "1";

	protected final String dontUsePrivate_protected = "2";

	public final String dontUsePrivate_public = "3";

	public static final String DONT_USE_PRIVATE = "4";

	public UsePrivateClass()
	{
	}

	public void dontUsePrivate()
	{
	}

	public static void dontUsePrivateStatic()
	{
	}

	// references
	public static void main(String[] args)
	{
		System.out.println(UsePrivateClass.USE_PRIVATE);
		UsePrivateClass.usePrivateStatic();
		UsePrivateClass example = new UsePrivateClass();
		example.usePrivate();
		System.out.println(example.usePrivate_default);
		System.out.println(example.usePrivate_protected);
		System.out.println(example.usePrivate_public);
		System.out.println(example.dontUsePrivate_private);
	}
}
