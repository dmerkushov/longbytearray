/*
 * Copyright 2017 Dmitriy Merkushov.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.dmerkushov.longbytearray;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A thread-safe non-dynamic array that can hold more than
 * {@link Integer#MAX_VALUE} elements
 *
 *
 * @author Dmitriy Merkushov <d.merkushov@gmail.com>
 */
public interface LongByteArray {

	/**
	 * Get the length of the array
	 *
	 * @return
	 */
	long getLength ();

	/**
	 * Get an element by its index
	 *
	 * @param index
	 * @return
	 */
	byte get (long index);

	/**
	 * Get a sub-array for this instance as a regular Java array. This method is
	 * designed to work faster than
	 * <code>longByteArray.getLongSubArray().toRegularArray()</code>
	 *
	 * @param off Start point for the sub-array
	 * @param len Length of the sub-array
	 * @return
	 * @see LongByteArray#getLongSubArray(long, long)
	 * @see LongByteArray#toRegularArray()
	 */
	byte[] getSubArray (long off, int len);

	/**
	 * Put an element to the instance. If a block for this element doesn't
	 * exist, it is initialized
	 *
	 * @param index
	 * @param element
	 */
	void put (long index, byte element);

	/**
	 * Create and get a regular Java array that contains the same values as this
	 * instance
	 *
	 * @return
	 * @throws IndexOutOfBoundsException if the length of the instance is too
	 * big for a regular Java array
	 */
	byte[] toRegularArray ();

	String toString ();

	/**
	 * Write the contents of this instance to an {@link OutputStream}
	 *
	 * @param outputStream
	 * @throws IOException for any internal I/O reason
	 */
	void write (OutputStream outputStream) throws IOException;

}
