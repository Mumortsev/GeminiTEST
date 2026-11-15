package ru.withbaby.data

import java.io.InputStream
import java.io.ByteArrayOutputStream

/** Bounded reading compatible with Android 8; no Java 9 InputStream APIs. */
fun InputStream.readLimited(limit: Int): ByteArray {
 val output=ByteArrayOutputStream();val buffer=ByteArray(8192)
 while(output.size()<limit) {val n=read(buffer,0,minOf(buffer.size,limit-output.size()));if(n<0) break;if(n>0) output.write(buffer,0,n)}
 return output.toByteArray()
}
