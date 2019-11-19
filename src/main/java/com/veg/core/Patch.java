package com.veg.core;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public final class Patch {
    private final int BUFFER = 4096;

    public Patch() {
    }

    public final boolean patchFile(String jarFilePath) {
        try {
            File jarFile = new File(jarFilePath);
            File temp = File.createTempFile("patch", (String)null);
            ZipInputStream zis = new ZipInputStream(new FileInputStream(jarFile));
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(temp));
            zos.setLevel(9);
            zos.setMethod(8);
            ZipEntry next = null;

            while((next = zis.getNextEntry()) != null) {
                ZipEntry current = new ZipEntry(next.getName());
                current.setComment(next.getComment());
                current.setTime(next.getTime());
                current.setExtra(next.getExtra());
                zos.putNextEntry(current);
                if (next.getName().equals("com/atlassian/extras/decoder/v2/Version2LicenseDecoder.class")) {
                    this.writeToStream(this.getClass().getResourceAsStream("/nl/invisible/keygen/patch/resources/Version2LicenseDecoder.class"), zos);
                } else {
                    this.writeToStream(zis, zos);
                }

                zis.closeEntry();
                zos.closeEntry();
            }

            zis.close();
            zos.flush();
            zos.close();
            temp.setLastModified(jarFile.lastModified());
            if (!jarFile.renameTo(new File(jarFile.getPath().substring(0, jarFile.getPath().lastIndexOf(46) + 1) + "bak"))) {
                return false;
            } else if (!temp.renameTo(jarFile)) {
                return false;
            } else {
                return true;
            }
        } catch (FileNotFoundException var8) {
            var8.printStackTrace();
            return false;
        } catch (IOException var9) {
            var9.printStackTrace();
            return false;
        }
    }

    private final void writeToStream(InputStream is, OutputStream os) {
        try {
            byte[] buffer = new byte[4096];

            int currentByte;
            while((currentByte = is.read(buffer)) != -1) {
                os.write(buffer, 0, currentByte);
            }
        } catch (IOException var5) {
            var5.printStackTrace();
        }

    }
}
