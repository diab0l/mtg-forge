package net.slightlymagic.braids.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Some general-purpose functions.
 */
public final class UtilFunctions {
	/**
	 * Do not instantiate.
	 */
	private UtilFunctions() {;}
	

	/**
	 * Throws a NullPointerException if param is null.
	 * 
	 * @param paramName  the name of the parameter; may be null
	 * @param param  the parameter to test
	 */
	public static void checkNotNull(String paramName, Object param) {
		if (param != null) return;
		
		NullPointerException exn = null;

		if (paramName != null) {
			exn = new NullPointerException(paramName + " must not be null");
		}
		else {
			// 
			exn = new NullPointerException();
		}
		
		// Doctor the exception to appear to come from the caller.
		StackTraceElement[] trace = exn.getStackTrace();
		int len = getSliceLength(trace, 1);
		exn.setStackTrace(slice(new StackTraceElement[len], trace, 1));
		throw exn;
	}
	

	/**
	 * Create an array from the (rest of) an iterator's output; 
	 * this function is horribly inefficient.
	 * 
	 * Please, only use it on small iterators.
	 * 
	 * @param iter the iterator to traverse
	 * 
	 * @return an array of (the rest of) the iterator's values
	 */
	public static <T> T[] iteratorToArray(Iterator<T> iter) {
		ArrayList<T> list = new ArrayList<T>();
		
		T item;
		while (iter.hasNext()) {
			item = iter.next();
			list.add(item);
		}
		
		@SuppressWarnings("unchecked")
		T[] result = (T[]) list.toArray();
		return result;
	}
	
	
	/**
	 * Returns the rightmost portion of an array, Python-style.
	 * 
	 * @param <T> (inferred automatically)
	 * 
	 * @param srcArray  the array to copy (shallowly)
	 * 
	 * @param startIndex  if positive, the index (from the left) at which to 
	 * start copying; if negative, we treat this as the index from the right.
	 * For example, calling this with startIndex = -2 returns the last two
	 * items in the array, if it has that many.
	 *  
	 * @return a shallow copy of array starting at startIndex; this may return
	 *         an empty array if the startIndex is out of bounds.
	 */
	public static <T extends Object> T[] slice(T[] dstArray, T[] srcArray, 
			int startIndex) 
	{
		if (startIndex < 0) {
			startIndex = srcArray.length + startIndex;
			if (startIndex < 0) startIndex = 0;
		}

		if (dstArray == null) {
			throw new NullPointerException();
		}
		
		if (srcArray == null) {
			throw new NullPointerException();
		}
		
		int resultLength = getSliceLength(srcArray, startIndex);
		
		if (dstArray.length != resultLength) {
			throw new ArrayIndexOutOfBoundsException(
			    "First parameter must have length " + resultLength + ", but length is " + dstArray.length + ".");
		}
		
		int srcIx = startIndex;
		
		for (int dstIx = 0; 
		     dstIx < resultLength && srcIx < srcArray.length; 
		     dstIx++, srcIx++) 
		{
			dstArray[dstIx] = srcArray[srcIx];
		}
		
		return dstArray;
	}

	
	/**
	 * Get a slice's length in preparation for taking a slice.
	 * 
	 * I do not like the fact that I have to use this function, but
	 * Java left me with little choice.
	 * 
	 * @see #slice(Object[], Object[], int)
	 * 
	 * @return the length of the array that would result from calling
	 * slice(Object[], Object[], int) with the given srcArray and
	 * startIndex.
	 */
	public static <T> int getSliceLength(T[] srcArray, int startIndex) {
		if (startIndex < 0) {
			startIndex = srcArray.length + startIndex;
			if (startIndex < 0) startIndex = 0;
		}

		int resultLength = srcArray.length - startIndex;
		return resultLength;
	}
	

	/**
	 * Handles the boilerplate null and isinstance check for an equals method.
	 * 
	 * Example:
	 * <pre>
	 * public boolean equals(Object obj) {
	 *     MyClassName that = checkNullOrNotInstance(this, obj);
	 *     if (that == null) {
	 *         return false;
	 *     }
	 *     //...
	 * }
	 * </pre> 
	 * 
	 * @param goodInstance  a non-null instance of type T; looks neater than
	 *        passing in goodInstance.getClass()
	 * 
	 * @param obj  the object to test
	 * 
	 * @return  null if obj is null or not an instance of goodInstance's class; 
	 *          otherwise, we return obj cast to goodInstance's type
	 */
	public static <T> T checkNullOrNotInstance(T goodInstance, Object obj) {
		if (goodInstance == null) {
			throw new NullPointerException("first parameter must not be null");
		}
		
		@SuppressWarnings("unchecked")
		Class<T> classT = (Class<T>) goodInstance.getClass();
		
		boolean viable = true;

		if (obj == null)  viable = false;
		else if (!(classT.isInstance(obj)))  viable = false;

		if (viable) {
			return classT.cast(obj);
		}
		return null;
	}

	
	
	/**
	 * Safely converts an object to a String.
	 * 
	 * @param obj  to convert; may be null
	 * 
	 * @return "null" if obj is null, obj.toString() otherwise
	 */
	public static String safeToString(Object obj) {
		if (obj == null) {
			return "null";
		}
		else {
			return obj.toString();
		}
	}

	/**
	 * Remove nulls and duplicate items from the list.
	 * 
	 * This may change the list's ordering. It uses the items' equals methods to
	 * determine equality.  
	 * 
	 * Advantages over HashSet: This consumes no unnecessary heap-memory, nor
	 * does it require objects to implement hashCode.  It is OK if 
	 * (o1.equals(o2) does not imply o1.hashCode() == o2.hashCode()).
	 * 
	 * Advantages over TreeSet: This does not require a comparator.  
	 * 
	 * Disadvantages over HashSet and TreeSet:  This runs in O(n*n) time.
	 * 
	 * @param list  the list to modify; this is fastest with ArrayList.
	 */
	public static <T> void smartRemoveDuplicatesAndNulls(List<T> list) 
	{
		// Get rid of pesky leading nulls.
		smartRemoveDuplicatesAndNullsHelper(list, 0, null);

		for (int earlierIx = 0; earlierIx < list.size(); earlierIx++) 
		{		
			for (int laterIx = earlierIx + 1; laterIx < list.size(); laterIx++) 
			{
				T itemAtEarlierIx = list.get(earlierIx);
				
				smartRemoveDuplicatesAndNullsHelper(list, laterIx,
						itemAtEarlierIx);
			}
				
		}
	}

	/**
	 * Helper method for smartRemoveDuplicatesAndNulls that is subject to 
	 * change; if you call this directly, you do so at your own risk!
	 * 
	 * @param list  the list to modify; if all items from startIx to the end
	 *  are either null or equal to objSeenPreviously, then we truncate the
	 *  list just before startIx.
	 * 
	 * @param startIx  the index to examine; we only move items within the range
	 *  of [startIx, list.size()-1].
	 *  
	 * @param objSeenPreviously  the object with which to compare list[startIx];
	 *  may be null.
	 */
	public static <T> void smartRemoveDuplicatesAndNullsHelper(
			List<T> list, int startIx, T objSeenPreviously) 
	{
		while (startIx < list.size() &&
			   (list.get(startIx) == null || 
			    list.get(startIx) == objSeenPreviously || 
			    list.get(startIx).equals(objSeenPreviously))) 
		{
			int lastItemIx = list.size()-1;
			
			// Overwrite the item at laterIx with the one at the end, 
			// then delete the one at the end.
			list.set(startIx, list.get(lastItemIx));
			list.remove(lastItemIx);
		}
	}

}
