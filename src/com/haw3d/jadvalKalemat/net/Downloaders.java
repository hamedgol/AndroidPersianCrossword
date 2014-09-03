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
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;

import com.haw3d.jadvalKalemat.BrowseActivity;
import com.haw3d.jadvalKalemat.PlayActivity;
import com.haw3d.jadvalKalemat.PuzzleDatabaseHelper;
import com.haw3d.jadvalKalemat.R;
import com.haw3d.jadvalKalemat.jadvalKalematApplication;

public class Downloaders {
    private static final Logger LOG = Logger.getLogger("com.haw3d.jadvalKalemat");
    private BrowseActivity context;
    private SharedPreferences prefs;
    private List<Downloader> downloaders = new LinkedList<Downloader>();
    private NotificationManager notificationManager;
    private boolean enableNotifications;
    private boolean enableIndividualDownloadNotifications;
    private Intent browseIntent;
    private PendingIntent pendingBrowseIntent;

    private static final int GENERAL_NOTIF_ID = 0;
    private static AtomicInteger nextNotifId = new AtomicInteger(1);

    public Downloaders(BrowseActivity context, NotificationManager notificationManager) {
        this.context = context;
        this.notificationManager = notificationManager;
        this.prefs = context.getPrefs();

        browseIntent = new Intent(Intent.ACTION_EDIT, null, context, BrowseActivity.class);
        pendingBrowseIntent = PendingIntent.getActivity(context, 0, browseIntent, 0);

        // BEQ has requested that his puzzles not be automatically downloaded
        //if (prefs.getBoolean("downloadBEQ", true)) {
        //    downloaders.add(new BEQDownloader());
        //}

        if (prefs.getBoolean("downloadAVXW", false)) {
            downloaders.add(new AVXWDownloader(prefs.getString("avxwUsername", ""), prefs.getString("avxwPassword", "")));
        }

        if (prefs.getBoolean("downloadAndyKravis", true)) {
            downloaders.add(new AndyKravisDownloader());
        }

        if (prefs.getBoolean("downloadCHE", true)) {
            downloaders.add(new CHEDownloader());
        }

        if (prefs.getBoolean("downloadCrooked", false)) {
            downloaders.add(new CrookedDownloader(prefs.getString("crookedUsername", ""), prefs.getString("crookedPassword", "")));
        }

        if (prefs.getBoolean("downloadCrosswordNation", false)) {
            downloaders.add(new CrosswordNationDownloader(prefs.getString("crosswordNationUsername", ""), prefs.getString("crosswordNationPassword", "")));
        }

        if (prefs.getBoolean("downloadErikAgard", true)) {
            downloaders.add(new ErikAgardDownloader());
        }

        if (prefs.getBoolean("downloadISwear", true)) {
            downloaders.add(new ISwearDownloader());
        }

        if (prefs.getBoolean("downloadInkwell", true)) {
            downloaders.add(new InkwellDownloader());
        }

        if (prefs.getBoolean("downloadJonesin", true)) {
            downloaders.add(new JonesinDownloader());
        }

        if (prefs.getBoolean("downloadJoseph", true)) {
            downloaders.add(new JosephDownloader());
        }

        if (prefs.getBoolean("downloadLAT", true)) {
            downloaders.add(new LATimesDownloader());
        }

        // Merl Reagle has requested this his puzzles be removed
        //if (prefs.getBoolean("downloadMerlReagle", true)) {
        //    downloaders.add(new MerlReagleDownloader());
        //}

        if (prefs.getBoolean("downloadMMMM",  true)) {
            downloaders.add(new MMMMDownloader());
        }

        if (prefs.getBoolean("downloadNevilleFogarty",  true)) {
            downloaders.add(new NevilleFogartyDownloader());
        }

        if (prefs.getBoolean("downloadNYT", false)) {
            downloaders.add(new NYTDownloader(
                prefs.getString("nytUsername", ""),
                prefs.getString("nytPassword", "")));
        }

        // NYT classic is no longer updating with new puzzles
        //if (prefs.getBoolean("downloadNYTClassic", true)) {
        //    downloaders.add(new NYTClassicDownloader());
        //}

        if (prefs.getBoolean("downloadNewsday", true)) {
            downloaders.add(new NewsdayDownloader());
        }

        if (prefs.getBoolean("downloadPatrickBlindauer",  true)) {
            downloaders.add(new PatrickBlindauerDownloader());
        }

        if (prefs.getBoolean("downloadPeople", true)) {
            downloaders.add(new PeopleScraper());
        }

        if (prefs.getBoolean("downloadPremier", true)) {
            downloaders.add(new PremierDownloader());
        }

        if (prefs.getBoolean("downloadSheffer", true)) {
            downloaders.add(new ShefferDownloader());
        }

        if (prefs.getBoolean("downloadUniversal", true)) {
            downloaders.add(new UniversalDownloader());
        }

        if (prefs.getBoolean("downloadUSAToday", true)) {
            downloaders.add(new USATodayDownloader());
        }

        if (prefs.getBoolean("downloadWsj", true)) {
            downloaders.add(new WSJDownloader());
        }

        if (prefs.getBoolean("downloadWaPo", true)) {
            downloaders.add(new WaPoDownloader());
        }

        if (prefs.getBoolean("downloadWaPoPuzzler", true)) {
            downloaders.add(new WaPoPuzzlerDownloader());
        }

        enableNotifications = prefs.getBoolean("enableNotifications", true);
        enableIndividualDownloadNotifications = enableNotifications && prefs.getBoolean("enableIndividualDownloadNotifications", true);
    }

    public List<Downloader> getDownloaders(Calendar date) {
        List<Downloader> retVal = new LinkedList<Downloader>();

        for (Downloader d : downloaders) {
            if (d.isPuzzleAvailable(date)) {
                retVal.add(d);
            }
        }

        return retVal;
    }

    public void download(Calendar date) {
        download(date, getDownloaders(date));
    }

    public void download(Calendar date, List<Downloader> downloaders) {
        date = (Calendar)date.clone();
        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);

        String contentTitle = context.getResources().getString(R.string.downloading_puzzles);
        Notification not = enableNotifications ? createDownloadingNotification(contentTitle) : null;
        boolean somethingDownloaded = false;

        if (!jadvalKalematApplication.makeDirs()) {
            return;
        }

        PuzzleDatabaseHelper dbHelper = jadvalKalematApplication.getDatabaseHelper();

        if (downloaders == null || downloaders.size() == 0) {
            downloaders = getDownloaders(date);
        }

        for (Downloader d : downloaders) {
            d.setContext(context);

            int notifId = nextNotifId.incrementAndGet();
            boolean succeeded = false;
            String failureMessage = null;

            try {
                updateDownloadingNotification(not, contentTitle, d.getName());

                if (enableNotifications && notificationManager != null) {
                    notificationManager.notify(GENERAL_NOTIF_ID, not);
                }

                String filename = d.getFilename(date);
                File downloadedFile = new File(jadvalKalematApplication.CROSSWORDS_DIR, filename);
                if (dbHelper.filenameExists(filename) || downloadedFile.exists()) {
                    LOG.info("Download skipped: " + filename);
                    continue;
                }

                if (downloadedFile.exists()) {
                    LOG.info("File already downloaded but not in database: " + downloadedFile);
                    dbHelper.addPuzzle(downloadedFile, d.getName(), d.sourceUrl(date), date.getTimeInMillis());
                    somethingDownloaded = true;
                    continue;
                }

                LOG.info("Download beginning: " + filename);

                d.download(date);

                LOG.info("Downloaded succeeded: " + filename);
                long id = dbHelper.addPuzzle(downloadedFile, d.getName(), d.sourceUrl(date), date.getTimeInMillis());
                if (id == -1) {
                    throw new IOException("Failed to load puzzle");
                }

                succeeded = true;
                somethingDownloaded = true;

                if (enableIndividualDownloadNotifications) {
                    postDownloadedNotification(notifId, d.getName(), downloadedFile, id);
                }

                context.postRenderMessage();
            } catch (DownloadException e) {
                LOG.warning("Download failed: " + d.getName());
                e.printStackTrace();
                failureMessage = context.getResources().getString(e.getResource());
            } catch (IOException e) {
                LOG.warning("Download failed: " + d.getName());
                e.printStackTrace();
            }

            // Notify the user about failed downloads.  Don't notify if
            // notifications are disabled, unless there's a non-standard
            // failure message (e.g. invalid username/password) that they
            // should know about.
            if (!succeeded &&
                (enableIndividualDownloadNotifications || failureMessage != null) &&
                notificationManager != null)
            {
                postDownloadFailedNotification(notifId, d.getName(), failureMessage);
            }
        }

        if (notificationManager != null && not != null) {
            notificationManager.cancel(GENERAL_NOTIF_ID);
        }

        if (somethingDownloaded && enableNotifications) {
            postDownloadedGeneral();
        }
    }

    public void enableIndividualDownloadNotifications(boolean enable) {
        this.enableIndividualDownloadNotifications = enable;
    }

    @SuppressWarnings("deprecation")
    private Notification createDownloadingNotification(String contentTitle) {
        return new Notification(android.R.drawable.stat_sys_download, contentTitle, System.currentTimeMillis());
    }

    @SuppressWarnings("deprecation")
    private void updateDownloadingNotification(Notification not, String contentTitle, String source) {
        if (not != null) {
            String contentText = context.getResources().getString(R.string.downloading_from, source);
            not.setLatestEventInfo(context, contentTitle, contentText, pendingBrowseIntent);
        }
    }

    @SuppressWarnings("deprecation")
    private void postDownloadedGeneral() {
        String contentTitle = context.getResources().getString(R.string.downloaded_new_puzzles_title);
        Notification not = new Notification(
                android.R.drawable.stat_sys_download_done, contentTitle,
                System.currentTimeMillis());

        String contentText = context.getResources().getString(R.string.downloaded_new_puzzles_text);
        not.setLatestEventInfo(context, contentTitle, contentText, pendingBrowseIntent);

        if (notificationManager != null) {
            notificationManager.notify(GENERAL_NOTIF_ID, not);
        }
    }

    @SuppressWarnings("deprecation")
    private void postDownloadedNotification(int notifId, String name, File puzFile, long puzzleId) {
        String contentTitle = context.getResources().getString(R.string.downloaded_puzzle_title, name);

        Notification not = new Notification(
                android.R.drawable.stat_sys_download_done, contentTitle,
                System.currentTimeMillis());

        Intent notificationIntent = new Intent(Intent.ACTION_EDIT, null, context, PlayActivity.class);
        notificationIntent.putExtra(PlayActivity.EXTRA_PUZZLE_ID, puzzleId);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
        not.setLatestEventInfo(context, contentTitle, puzFile.getName(), contentIntent);

        if (notificationManager != null) {
            notificationManager.notify(notifId, not);
        }
    }

    @SuppressWarnings("deprecation")
    private void postDownloadFailedNotification(int notifId, String name, String failureMessage) {
        String contentTitle = context.getResources().getString(R.string.download_failed, name);

        String contentText = (failureMessage != null ? failureMessage : name);

        Notification not = new Notification(
                android.R.drawable.stat_notify_error, contentTitle,
                System.currentTimeMillis());
        not.setLatestEventInfo(context, contentTitle, contentText, pendingBrowseIntent);

        if (this.notificationManager != null) {
            this.notificationManager.notify(notifId, not);
        }
    }
}
