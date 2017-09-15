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
import java.util.Objects;

/**
 * An implementation of {@link LongByteArray} that is linked to another
 * LongByteArray as its part with the given offset and length
 *
 * @author dmerkushov
 */
public final class LinkedLongByteArray implements LongByteArray {

	private final LongByteArray linkedToArray;
	private final long offset;
	private final long length;

	public LinkedLongByteArray (LongByteArray linkedToArray, long offset, long length) {
		Objects.requireNonNull (linkedToArray, "linkedToArray");

		if (offset < 0) {
			throw new ArrayIndexOutOfBoundsException ("offset < 0: " + offset);
		}
		if (length < 0) {
			throw new ArrayIndexOutOfBoundsException ("length < 0: " + length);
		}
		if (offset + length > linkedToArray.getLength ()) {
			throw new ArrayIndexOutOfBoundsException ("offset " + offset + " + length " + length + " > linkedToArray.length" + linkedToArray.getLength ());
		}

		this.linkedToArray = linkedToArray;
		this.offset = offset;
		this.length = length;
	}

	@Override
	public String toString () {
		return getClass ().getCanonicalName () + "[0," + (length - 1) + "] - linked at offset " + offset + " to " + linkedToArray.toString ();
	}

	@Override
	public byte[] toRegularArray () {
		if (length > Integer.MAX_VALUE) {
			throw new IndexOutOfBoundsException ("Cannot get a regular array of size " + length + ", it is more than the integer max value " + Integer.MAX_VALUE);
		}

		return linkedToArray.getSubArray (offset, (int) length);
	}

	@Override
	public void write (OutputStream outputStream) throws IOException {
		for (long i = 0; i < length; i++) {
			outputStream.write (Byte.toUnsignedInt (get (i)));
		}
	}

	@Override
	public byte[] getSubArray (long off, int len) {
		return linkedToArray.getSubArray (offset + off, len);
	}

	@Override
	public void put (long index, byte element) {
		linkedToArray.put (offset + index, element);
	}

	@Override
	public byte get (long index) {
		return linkedToArray.get (offset + index);
	}

	@Override
	public long getLength () {
		return length;
	}

	public long getOffset () {
		return offset;
	}

	public LongByteArray getLinkedTo () {
		return linkedToArray;
	}

}
