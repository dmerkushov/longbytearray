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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A basic implementation of {@link LongByteArray}. It is divided into blocks of
 * given size ({@link LongArray#BLOCKSIZE_DEFAULT} by default) that are
 * initialized lazily, when a value in a new block is added
 *
 *
 * @author Dmitriy Merkushov <d.merkushov@gmail.com>
 */
public class LongByteArrayImpl implements LongByteArray {

	/**
	 * The default size of an array block
	 */
	public static final int BLOCKSIZE_DEFAULT = 1000;

	/**
	 * The total length of this instance
	 */
	private final long length;

	/**
	 * The block size for this instance
	 */
	final int blockSize;

	/**
	 * The map where blocks are stored
	 */
	final Map<Long, byte[]> arrays;

	/**
	 * Initialize an instance with the given regular array
	 *
	 * @param originalArray The array holding the elements. Must not be null
	 */
	public LongByteArrayImpl (byte[] originalArray) {
		this (originalArray.length, originalArray.length);

		arrays.put (0L, Arrays.copyOf (originalArray, originalArray.length));
	}

	/**
	 * Initialize an instance with the {@link #BLOCKSIZE_DEFAULT default} block
	 * size
	 *
	 * @param length The required length of this instance
	 */
	public LongByteArrayImpl (long length) {
		this (length, BLOCKSIZE_DEFAULT);
	}

	/**
	 * Initialize an instance with the given block size
	 *
	 * @param length The required length of this instance
	 * @param blocksize The block size for this instance
	 */
	public LongByteArrayImpl (long length, int blocksize) {

		if (length < 0) {
			throw new IllegalArgumentException ("length < 0: " + length);
		}

		this.length = length;
		this.blockSize = blocksize;

		this.arrays = new HashMap<> ();
	}

	/**
	 * Get an element by its index
	 *
	 * @param index
	 * @return
	 */
	@Override
	public byte get (long index) {
		long blockIndex = blockIndex (index);
		synchronized (arrays) {
			if (arrays.containsKey (blockIndex)) {
				return arrays.get (blockIndex)[indexInBlock (index)];
			}
		}
		return 0;
	}

	/**
	 * Put an element to the instance. If a block for this element doesn't
	 * exist, it is initialized
	 *
	 * @param index
	 * @param element
	 */
	@Override
	public void put (long index, byte element) {
		long blockIndex = blockIndex (index);

		synchronized (arrays) {
			if (!arrays.containsKey (blockIndex)) {
				arrays.put (blockIndex, new byte[blockSize]);
			}

			arrays.get (blockIndex)[indexInBlock (index)] = element;
		}
	}

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
	@Override
	public byte[] getSubArray (long off, int len) {
		if (off < 0 || off >= length) {
			throw new ArrayIndexOutOfBoundsException ("Offset " + off + " is out of bounds: 0 to " + (length - 1));
		}
		if (len < 0) {
			throw new IllegalArgumentException ("Desired sub-array length is less than 0: " + len);
		}

		long start = Math.max (0, off);
		long startBlockIndex = blockIndex (start);
		int startIndexInBlock = indexInBlock (start);

		long end = Math.min (off + len, length);
		long endBlockIndex = blockIndex (end);
		int endIndexInBlock = indexInBlock (end);

		byte[] sub = new byte[len];
		int subArrayPosition = 0;
		synchronized (arrays) {
			for (long i = startBlockIndex; i <= endBlockIndex; i++) {
				int currentBlockStart = (i == startBlockIndex ? startIndexInBlock : 0);
				int currentBlockEnd = (i == endBlockIndex ? endIndexInBlock : blockSize);
				byte[] internalArray = arrays.get (i);
				if (internalArray == null) {
					internalArray = new byte[blockSize];
					Arrays.fill (internalArray, (byte) 0);
				}
				System.arraycopy (internalArray, currentBlockStart, sub, subArrayPosition, currentBlockEnd - currentBlockStart);
				subArrayPosition += blockSize;
			}
		}

		return sub;
	}

	/**
	 * Write the contents of this instance to an {@link OutputStream}
	 *
	 * @param outputStream
	 * @throws IOException for any internal I/O reason
	 */
	@Override
	public void write (OutputStream outputStream) throws IOException {
		Objects.requireNonNull (outputStream, "outputStream");

		if (length == 0) {
			return;
		}

		long maxBlockIndex = maxBlockIndex ();
		synchronized (arrays) {
			for (long i = 0; i <= maxBlockIndex; i++) {
				byte[] internalArray = arrays.get (i);
				if (internalArray == null) {
					internalArray = new byte[blockSize];
					Arrays.fill (internalArray, (byte) 0);
				}

				if (i < maxBlockIndex || (indexInBlock (length - 1) + 1) == blockSize) {
					outputStream.write (internalArray);
				} else {
					outputStream.write (internalArray, 0, indexInBlock (length - 1) + 1);
				}
			}
		}
	}

	/**
	 * Create and get a regular Java array that contains the same values as this
	 * instance
	 *
	 * @return
	 * @throws IndexOutOfBoundsException if the length of the instance is too
	 * big for a regular Java array
	 */
	@Override
	public byte[] toRegularArray () {
		if (length > Integer.MAX_VALUE) {
			throw new IndexOutOfBoundsException ("Cannot get a regular array of size " + length + ", it is more than the integer max value " + Integer.MAX_VALUE);
		}

		return getSubArray (0, (int) length);
	}

	/**
	 * Get the fill factor for the internal block structure. The fill factor is
	 * the actual block count divided by theoretical maximum count.
	 *
	 * @return
	 */
	public double getFillFactor () {
		if (length == 0) {
			return 1.0;
		}

		double maxBlockIndex = maxBlockIndex ();
		double blockCount;
		synchronized (arrays) {
			blockCount = arrays.size ();
		}
		return blockCount / (maxBlockIndex + 1.0);
	}

	long maxBlockIndex () {
		if (length <= 0) {
			return 0;
		}

		return blockIndex (length - 1);
	}

	long blockIndex (long index) {
		if (index < 0 || index >= length) {
			throw new ArrayIndexOutOfBoundsException ("This instance's bounds: 0 to " + (length - 1) + ", requested index: " + index);
		}

		return index / blockSize;
	}

	int indexInBlock (long index) {
		if (index < 0 || index >= length) {
			throw new ArrayIndexOutOfBoundsException ("This instance's bounds: 0 to " + (length - 1) + ", requested index: " + index);
		}

		return (int) (index % blockSize);
	}

	@Override
	public String toString () {
		return getClass ().getCanonicalName () + "[0," + (length - 1) + "]";
	}

	@Override
	public long getLength () {
		return length;
	}

	/**
	 * Get the block size for this instance
	 *
	 * @return
	 */
	public int getBlockSize () {
		return blockSize;
	}

}
