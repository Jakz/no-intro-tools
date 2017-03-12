package com.github.jakz.nit.tests;

import java.io.IOException;
import java.io.RandomAccessFile;

import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchive;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;

public class Tests
{
  public static void main(String[] args) {

    RandomAccessFile randomAccessFile = null;
    IInArchive inArchive = null;
    try {
        randomAccessFile = new RandomAccessFile("/Volumes/RAMDisk/Archive.7z", "r");
        inArchive = SevenZip.openInArchive(null, // autodetect archive type
                new RandomAccessFileInStream(randomAccessFile));

        // Getting simple interface of the archive inArchive
        ISimpleInArchive simpleInArchive = inArchive.getSimpleInterface();

        System.out.println("   Size   | Compr.Sz. | Filename");
        System.out.println("----------+-----------+---------");

        for (ISimpleInArchiveItem item : simpleInArchive.getArchiveItems()) {
            System.out.println(String.format("%9s | %9s | %s", // 
                    item.getSize(), 
                    item.getPackedSize(), 
                    item.getPath()));
        }
    } catch (Exception e) {
        System.err.println("Error occurs: " + e);
    } finally {
        if (inArchive != null) {
            try {
                inArchive.close();
            } catch (SevenZipException e) {
                System.err.println("Error closing archive: " + e);
            }
        }
        if (randomAccessFile != null) {
            try {
                randomAccessFile.close();
            } catch (IOException e) {
                System.err.println("Error closing file: " + e);
            }
        }
    }
}
}
