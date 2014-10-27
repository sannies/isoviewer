/*
 * Copyright 2014 Sebastian Annies
 *
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an AS IS BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.sannies.isoviewer;

import java.io.UnsupportedEncodingException;

/**
 *
 */
public final class Iso8859_1 {


  public static String convert(byte[] b) {
    try {
      return new String(b, "ISO-8859-1");
    } catch (UnsupportedEncodingException e) {
      throw new Error(e);
    }
  }

}
