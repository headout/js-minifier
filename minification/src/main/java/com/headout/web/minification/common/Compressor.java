package com.headout.web.minification.common;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.tagext.BodyContent;

import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.JSError;
import com.google.javascript.jscomp.Result;
import com.google.javascript.jscomp.SourceFile;
import com.headout.web.minification.exception.CommonRuntimeException;
import com.headout.web.minification.exception.util.ExceptionUtil;

/**
 * This class is used for minifying JS/CSS code. This class uses google closure compiler to compile the JS code. CSS
 * code is already minified.
 */
public class Compressor {
	/*
	 * Does compilation of JS code using google closure compiler. Throws runtime exception if any error occures.
	 */

	public static enum ContentType {
		JS, CSS;
	}

	/**
	 * Calulates absolutepath of src . If src is null then returns absolutepath of requesting JSP file
	 *
	 * @param request httpRequest
	 * @param externalRelativePath relative filepath
	 * @return String specifying absolute filepath
	 */
	public static String getAbsolutePath(HttpServletRequest request, String externalRelativePath) {
		// FIXME: Change this to Relative Path. It's a security breach if the absolute paths get out into logs and out
		// Moreover, each file can be uniquely identified using the relative path itslelf within a web context. Hence we
		// can use the relative paths for caching and uniquely identifying files (JSP IDs can be used depending upon
		// usecase).
		// You might need to use absolute path to get the file content, nevertheless, it should not be stored and
		// only be used for fetching file data.

		// TODO: Not that Important.
		// See if this also works for src paths which are relative to the current URL eg: "./xyz" & "../xyz". It doesn't
		// happen in our case so it's not breaking our system yet.

		// If src is null means we need path of jspFile
		if (externalRelativePath == null) {
			String jspFilePath = request.getServletPath();
			return request.getServletContext().getRealPath(jspFilePath);
		}
		// else we need the absolute path of the file which has the input src relative path.
		else
			return request.getServletContext().getRealPath(externalRelativePath);
	}

	/**
	 *
	 * @param path filepath
	 * @return filecontent in string format
	 * @throws IOException
	 */
	public static String getExternalCode(String path) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, "UTF-8");
	}

	public static boolean isWebAddress(String path) {
		return path.startsWith("http://") || path.startsWith("https://") || path.startsWith("//");
	}

	public JSError[] errors;

	public String afterMinified;

	/**
	 * this function compiles JS code. It uses google closure library.
	 *
	 * @param key unique key is required, for abstract source file to be referenced, for logging errors.
	 * @param jsCode JS code that needs to be compiled
	 *
	 */

	public void compile(String key, String jsCode) {
		Compiler compiler = new Compiler();
		CompilerOptions options = new CompilerOptions();

		// simple optimization mode is used here
		// there exists advanced optimization mode also. But for compiling in advanced mode we need all JS code at same
		// place.
		// as in advanced mode it minifies global variables, function names also.
		CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel(options);

		// External variables are declared in 'externs' files. For instance, the file may include definitions for global
		// javascript/browser objects such as window, document.
		SourceFile extern = SourceFile.fromCode("externs.js", "");

		// The dummy input name key is used here so that any warnings or
		// errors will cite line numbers in terms of key.
		SourceFile input = SourceFile.fromCode(key, jsCode);

		// compile() returns a Result, but it is not needed here.
		Result result = compiler.compile(extern, input, options);

		// If there are errors , then set errors.
		this.errors = result.errors;

		// The compiler is responsible for generating the compiled code; it is not
		// accessible via the Result.
		// set afterMinified body content
		this.afterMinified = compiler.toSource();
	}

	/**
	 * This method returns minified code for both 'inline js code' or 'external JS/CSS file'.. If file is not modified
	 * then cached minified JS/CSS code is returned. If file is modified then cache is updated with modified minified
	 * content and then updated cache content is set for a minified content to be returned.
	 *
	 * @param absolutepath its path of JSP file if its
	 * @param isInline
	 */
	public void minify(String absolutepath, BodyContent bodyContent, String jspID, Boolean isInline,
			ContentType contentType) {
		String beforeMinified = null;

		// generate key , key generation logic differs depending on is it inline js code or external js code
		// get keyInstance which is mapped with key
		CacheKey keyInstance = CacheKey.getInstance(CacheKey.generateKey(absolutepath, jspID, isInline));
		File f = new File(absolutepath);

		// get locks
		ReentrantReadWriteLock rwLock = keyInstance.getLock();
		ReadLock rlock = rwLock.readLock();

		boolean unlock = false;
		try {
			// Create a read Lock to read lastModified timing of file .
			rlock.lock();

			// If file is modified then we need to update cache content
			if (f.lastModified() > keyInstance.getLastModified()) {

				rlock.unlock();
				unlock = true;

				// Get Inline JS code content.
				if (isInline)
					beforeMinified = bodyContent.getString();
				else
					// fetch from JS/CSS file
					beforeMinified = getExternalCode(absolutepath);

				// JS code we need to compile(minify)
				// compile function sets afterMinified as minified code
				if (contentType == ContentType.JS)
					compile(keyInstance.getKey(), beforeMinified);
				// CSS is already minified
				else
					afterMinified = beforeMinified;

				// Updates minified body in cache, and returns cache content.
				afterMinified = keyInstance.updateMinifiedBodyIfApplicable(f.lastModified(), afterMinified);
			} else
				// set cached content as the file has not been modified
				afterMinified = keyInstance.getMinifiedCode();
		} catch (NoSuchFileException ex) {
			// Couldn't find the file. This is a UI level error and our code should not fail because of that.
			// Just log a warning and assign the content as EMPTY.
			System.out.println("WARNING: File to be minified was not found. Ignoring exception and using EMPTY data.\n"
					+ ExceptionUtil.getStackTraceString(ex));
			afterMinified = "";
		} catch (IOException ex) {
			throw new CommonRuntimeException(ex, "An Error has occurred, while getting content from file.");
		} finally {
			if (!unlock)
				rlock.unlock();
		}
	}

	/**
	 * This function is used for minifying Inline JS code for non-production environment. This function compiles JS each
	 * and every time request is processed. No caching is done.
	 *
	 * @param key String representing unique key
	 * @return returns minified JS code in string format
	 */
	public void minifyOnFly(BodyContent bodyContent, String key) {
		String beforeMinified = bodyContent.getString();
		compile(key, beforeMinified);
	}

}
