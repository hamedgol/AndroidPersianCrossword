/**
 * This file is part of Words With Crosses.
 *
 * Copyright (C) 2009-2010 Robert Cooper
 * Copyright (C) 2013 Adam Rosenfield
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.haw3d.jadvalKalemat.net;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import android.content.Context;

import com.haw3d.jadvalKalemat.jadvalKalematApplication;
import com.haw3d.jadvalKalemat.versions.AndroidVersionUtils;

import org.apache.http.protocol.HttpContext;

public abstract class AbstractDownloader implements Downloader {

    protected static final Logger LOG = Logger.getLogger("com.haw3d.jadvalKalemat");

    protected static final Map<String, String> EMPTY_MAP = Collections.<String, String>emptyMap();

    protected String baseUrl;
    private String downloaderName;

    protected final AndroidVersionUtils utils = AndroidVersionUtils.Factory.getInstance();

    protected static final String[] SHORT_MONTHS = new String[] {
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };

    protected static final NumberFormat DEFAULT_NF;

    private static final String SCRUB_URL_REGEX = "\\b(username|password)=[^&]*";
    private static final Pattern SCRUB_URL_PATTERN = Pattern.compile(SCRUB_URL_REGEX);

    static {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumIntegerDigits(2);
        nf.setMaximumFractionDigits(0);
        DEFAULT_NF = nf;
    }

    protected static Calendar createDate(int year, int month, int day) {
        Calendar date = Calendar.getInstance();
        date.clear();
        date.set(year, month - 1, day);  // Months start at 0 for Calendar!
        return date;
    }

    protected AbstractDownloader(String baseUrl, String downloaderName) {
        this.baseUrl = baseUrl;
        this.downloaderName = downloaderName;
    }

    public void setContext(Context ctx) {
        this.utils.setContext(ctx);
    }

    public String getFilename(Calendar date) {
        return (date.get(Calendar.YEAR) +
                "-" +
                DEFAULT_NF.format(date.get(Calendar.MONTH) + 1) +
                "-" +
                DEFAULT_NF.format(date.get(Calendar.DAY_OF_MONTH)) +
                "-" +
                this.downloaderName.replaceAll(" ", "").replace("/", "_") +
                ".puz");
    }

    public String sourceUrl(Calendar date) {
        return this.baseUrl + this.createUrlSuffix(date);
    }

    @Override
    public String toString() {
        return getName();
    }

    public String getName() {
        return downloaderName;
    }

    protected abstract String createUrlSuffix(Calendar date);

    public void download(Calendar date) throws IOException {
        download(date, createUrlSuffix(date));
    }

    protected void download(Calendar date, String urlSuffix) throws IOException {
        download(date, urlSuffix, EMPTY_MAP);
    }

    protected void download(Calendar date, String urlSuffix, Map<String, String> headers) throws IOException {
        download(date, urlSuffix, headers, null);
    }

    protected void download(Calendar date, String urlSuffix, Map<String, String> headers, HttpContext httpContext) throws IOException {
        URL url = new URL(this.baseUrl + urlSuffix);

        LOG.info("Downloading " + scrubUrl(url));

        String filename = getFilename(date);
        File destFile = new File(jadvalKalematApplication.CROSSWORDS_DIR, filename);
        utils.downloadFile(url, headers, destFile, true, getName(), httpContext);
    }

    protected String downloadUrlToString(String url) throws IOException {
        return downloadUrlToString(url, EMPTY_MAP);
    }

    protected String downloadUrlToString(String url, Map<String, String> headers) throws IOException {
        return downloadUrlToString(url, headers, null);
    }

    protected String downloadUrlToString(String url, Map<String, String> headers, HttpContext httpContext) throws IOException {
        LOG.info("Downloading to string: " + url);

        return utils.downloadToString(new URL(url), headers, httpContext);
    }

    /**
     * If relativeUrl is an absolute URL, then it is returned unchanged.
     * Otherwise, this resolves relativeUrl against baseUrl and returns the
     * resulting absolute URL.
     */
    public static String resolveUrl(String baseUrl, String relativeUrl) throws MalformedURLException
    {
        return new URL(new URL(baseUrl), relativeUrl).toString();
    }

    public static String scrubUrl(String url)
    {
        return SCRUB_URL_PATTERN.matcher(url).replaceAll("$1=[redacted]");
    }

    public static String scrubUrl(URL url)
    {
        return scrubUrl(url.toString());
    }
}
