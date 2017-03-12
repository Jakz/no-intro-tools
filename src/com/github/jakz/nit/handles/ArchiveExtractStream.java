package com.github.jakz.nit.handles;

import java.io.IOException;

import net.sf.sevenzipjbinding.ISequentialOutStream;

interface ArchiveExtractStream extends ISequentialOutStream
{
  void close() throws IOException;
}