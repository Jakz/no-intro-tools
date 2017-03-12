package com.github.jakz.nit.merger;

import java.io.InputStream;

public abstract class CompressorEntry
{
  abstract String name();
  abstract long size();
  abstract InputStream getInputStream();
}
