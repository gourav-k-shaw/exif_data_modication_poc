

package com.example.demo;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import ch.qos.logback.core.util.FileUtil;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.ImagingException;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.common.RationalNumber;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;
import org.apache.commons.io.FileUtils;

import org.apache.commons.imaging.formats.tiff.constants.GpsTagConstants;
import org.apache.commons.imaging.formats.tiff.fieldtypes.FieldTypeRational;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputField;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;


import javax.imageio.plugins.tiff.ExifGPSTagSet;
import java.io.*;
import java.util.Arrays;
import java.util.Base64;

public class ImageBase64ExampleWithExif {

	public static void main(String[] args) {
		String inputImagePath = "/Users/gourav/Desktop/anubhab.jpeg";
		String outputImagePath = "/Users/gourav/Desktop/virat_kohli.jpeg";

		try {
			// Read the image from the file path into an InputStream
			InputStream inputStream = new FileInputStream(inputImagePath);
			System.out.println(inputStream);

			// Modify the geolocation data
			InputStream modifiedImageStream = removeExif(inputStream, 200.0, 200.0);

			// Save the modified image to the specified output path
			saveInputStreamAsImage(modifiedImageStream, outputImagePath);

			System.out.println("Image processed and saved successfully.");

		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
	}

	public static void saveInputStreamAsImage(InputStream inputStream, String outputImagePath) throws IOException {
		// Write the InputStream to a file
		byte[] buffer = new byte[1024];
		int bytesRead;
		FileOutputStream outputStream = new FileOutputStream(outputImagePath);
		while ((bytesRead = inputStream.read(buffer)) != -1) {
			outputStream.write(buffer, 0, bytesRead);
		}
		outputStream.close();
	}


	public static InputStream removeExif(InputStream inputStream, double lat, double lon) throws Exception {
		File tempFile = File.createTempFile("temp", ".jpg");
		try (FileOutputStream fos = new FileOutputStream(tempFile)) {
			byte[] buffer = new byte[1024];
			int bytesRead;
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				fos.write(buffer, 0, bytesRead);
			}
		}

		// Create an output file for the modified image
		File outputFile = File.createTempFile("output", ".jpg");

		// Change EXIF metadata to modify GPS info
		changeExifMetadata(tempFile, outputFile, lat, lon);

		// Return the modified image as InputStream
		return new FileInputStream(outputFile);
	}

	public static void changeExifMetadata(final File jpegImageFile, final File dst, double lat, double lon) throws IOException, ImagingException {

		try (FileOutputStream fos = new FileOutputStream(dst);
			 OutputStream os = new BufferedOutputStream(fos)) {

			TiffOutputSet outputSet = null;

			// note that metadata might be null if no metadata is found.
			final ImageMetadata metadata = Imaging.getMetadata(jpegImageFile);
			final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
			if (null != jpegMetadata) {
				// note that exif might be null if no Exif metadata is found.
				final TiffImageMetadata exif = jpegMetadata.getExif();

				if (null != exif) {

					outputSet = exif.getOutputSet();
				}
			}
			if (null == outputSet) {
				FileUtils.copyFile(jpegImageFile, dst);
				return;
			}
			/*{
				outputSet.setGPSInDegrees(lat, lon);
			}*/
			outputSet.removeField(ExifTagConstants.EXIF_TAG_GPSINFO);
			final TiffOutputDirectory exifDirectory = outputSet.getExifDirectory();
			if (null != exifDirectory) {
				exifDirectory.removeField(ExifTagConstants.EXIF_TAG_GPSINFO);
			}
			new ExifRewriter().updateExifMetadataLossless(jpegImageFile, os, outputSet);
		}
	}
}