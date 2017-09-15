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
import java.io.InputStream;
import java.util.Objects;

/**
 *
 * @author dmerkushov
 */
public class LongByteArrayReader {

	/**
	 * Read an instance from an {@link InputStream}. The instance is initialized
	 * with the given length and default block size. If the end of the input
	 * stream is reached before the array is read completely, reading will be
	 * broken with an {@link IOException}
	 *
	 * @param inputStream
	 * @param length
	 * @return
	 * @throws IOException if the end of the input stream is reached
	 * unexpectedly, or any other internal I/O reason
	 */
	public static LongByteArrayImpl read (InputStream inputStream, long length) throws IOException {
		return read (inputStream, length, LongByteArrayImpl.BLOCKSIZE_DEFAULT);
	}

	/**
	 * Read an instance from an {@link InputStream}. The instance is initialized
	 * with the given length and block size. If the end of the input stream is
	 * reached before the array is read completely, reading will be broken with
	 * an {@link IOException}
	 *
	 * @param inputStream
	 * @param length
	 * @param blockSize
	 * @return
	 * @throws IOException if the end of the input stream is reached
	 * unexpectedly, or any other internal I/O reason
	 */
	public static LongByteArrayImpl read (InputStream inputStream, long length, int blockSize) throws IOException {
		Objects.requireNonNull (inputStream, "inputStream");

		LongByteArrayImpl arr = new LongByteArrayImpl (length, blockSize);
		long currentPosition = 0;
		long blockIndex = 0;
		synchronized (arr.arrays) {
			while (currentPosition < length) {
				blockIndex = arr.blockIndex (currentPosition);
				byte[] internalArray = arr.arrays.get (blockIndex);
				if (internalArray == null) {
					internalArray = new byte[arr.blockSize];
					arr.arrays.put (blockIndex, internalArray);
				}
				int currentPositionInBlock = arr.indexInBlock (currentPosition);
				int readCount = inputStream.read (internalArray, currentPositionInBlock, arr.blockSize - currentPositionInBlock);
				if (readCount < 0) {
					throw new IOException ("End of input stream is reached before the whole LongByteArray instance could be read. Position: " + currentPosition + ", desired length: " + length);
				}
				currentPosition += readCount;
			}
		}
		return arr;
	}

}
