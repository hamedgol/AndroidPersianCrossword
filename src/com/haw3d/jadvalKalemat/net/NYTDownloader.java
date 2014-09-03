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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import com.haw3d.jadvalKalemat.R;
import com.haw3d.jadvalKalemat.jadvalKalematApplication;
import com.haw3d.jadvalKalemat.io.IO;

/**
 * New York Times
 * URL: http://www.nytimes.com/premium/xword/YYYY/MM/DD/[Mon]DDYY.puz
 * Date: Daily
 */
public class NYTDownloader extends AbstractDownloader {
    public static final String NAME = "New York Times";
    private static final String LOGIN_URL = "https://myaccount.nytimes.com/auth/login?URI=http://select.nytimes.com/premium/xword/puzzles.html";
    private HashMap<String, String> params = new HashMap<String, String>();

    protected NYTDownloader(String username, String password) {
        super("http://www.nytimes.com/premium/xword/", NAME);
        params.put("is_continue", "false");
        params.put("userid", username);
        params.put("password", password);
    }

    public boolean isPuzzleAvailable(Calendar date) {
        return true;
    }

    @Override
    protected String createUrlSuffix(Calendar date) {
        return (date.get(Calendar.YEAR) + "/" +
                DEFAULT_NF.format(date.get(Calendar.MONTH) + 1) +
                "/" +
                DEFAULT_NF.format(date.get(Calendar.DAY_OF_MONTH)) +
                "/" +
                SHORT_MONTHS[date.get(Calendar.MONTH)] +
                DEFAULT_NF.format(date.get(Calendar.DAY_OF_MONTH)) +
                DEFAULT_NF.format(date.get(Calendar.YEAR) % 100) +
                ".puz");
    }

    @Override
    protected void download(Calendar date, String urlSuffix) throws IOException {
        login();

        URL url = new URL(this.baseUrl + urlSuffix);
        LOG.info("NYT: Downloading " + url);

        HttpGet get = new HttpGet(url.toString());
        HttpResponse response = utils.getHttpClient().execute(get);

        int status = response.getStatusLine().getStatusCode();
        if (status != 200) {
            LOG.warning("NYT: Download failed: " + response.getStatusLine());
            throw new IOException("Download failed: status " + status);
        }

        File tempFile = new File(jadvalKalematApplication.TEMP_DIR, getFilename(date));
        FileOutputStream fos = new FileOutputStream(tempFile);
        try {
            IO.copyStream(response.getEntity().getContent(), fos);
        } finally {
            fos.close();
        }

        File destFile = new File(jadvalKalematApplication.CROSSWORDS_DIR, getFilename(date));
        if (!tempFile.renameTo(destFile)) {
            throw new IOException("Failed to rename " + tempFile + " to " + destFile);
        }
    }

    private void login() throws IOException {
        HttpClient httpClient = utils.getHttpClient();

        HttpGet httpget = new HttpGet(LOGIN_URL);

        LOG.info("NYT: Logging in");
        HttpResponse response = httpClient.execute(httpget);
        HttpEntity entity = response.getEntity();

        if (entity != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            entity.writeTo(baos);

            String resp = new String(baos.toByteArray());
            String tok = "name=\"token\" value=\"";
            String expires = "name=\"expires\" value=\"";
            int tokIndex = resp.indexOf(tok);

            if (tokIndex != -1) {
                params.put(
                        "token",
                        resp.substring(tokIndex + tok.length(),
                                resp.indexOf("\"", tokIndex + tok.length())));
            } else {
                LOG.warning("NYT: Failed to parse token in login page");
            }

            int expiresIndex = resp.indexOf(expires);

            if (expiresIndex != -1) {
                params.put(
                        "expires",
                        resp.substring(
                                expiresIndex + expires.length(),
                                resp.indexOf("\"",
                                        expiresIndex + expires.length())));
            } else {
                LOG.warning("NYT: Failed to parse expires in login page");
            }
        }

        HttpPost httpPost = new HttpPost(LOGIN_URL);

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();

        for (Entry<String, String> e : this.params.entrySet()) {
            nvps.add(new BasicNameValuePair(e.getKey(), e.getValue()));
        }

        httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

        response = httpClient.execute(httpPost);
        entity = response.getEntity();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        if (entity != null) {
            entity.writeTo(baos);

            String resp = new String(baos.toByteArray());

            if (resp.indexOf("Log in to manage") != -1) {
                LOG.warning("NYT: Password error");
                throw new DownloadException(R.string.login_failed);
            }
        }

        LOG.info("NYT: Logged in");
    }
}
