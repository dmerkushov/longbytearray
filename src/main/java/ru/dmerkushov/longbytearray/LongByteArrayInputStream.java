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
 * An {@link InputStream} to read from a {@link LongByteArray}
 *
 * @author dmerkushov
 */
public class LongByteArrayInputStream extends InputStream {

	LongByteArray internalArray;
	long markedPosition;
	long position = 0;

	public LongByteArrayInputStream (LongByteArray internalArray) {
		Objects.requireNonNull (internalArray, "internalArray");

		this.internalArray = internalArray;
	}

	@Override
	public int read () throws IOException {
		if (position >= internalArray.getLength ()) {
			return -1;
		}

		int result = Byte.toUnsignedInt (internalArray.get (position));
		position++;
		return result;
	}

}
