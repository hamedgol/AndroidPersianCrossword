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


package com.haw3d.jadvalKalemat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.haw3d.jadvalKalemat.BrowseActivity.Provider;
import com.haw3d.jadvalKalemat.net.Downloader;
import com.haw3d.jadvalKalemat.net.Downloaders;
import com.haw3d.jadvalKalemat.net.DummyDownloader;


/**
 * Custom dialog for choosing puzzles to download.
 */

public class DownloadPickerDialogBuilder {
    private static DateFormat DATE_FORMAT = new SimpleDateFormat("EEEE", Locale.getDefault());
    private Activity mActivity;
    private Dialog mDialog;
    private List<Downloader> mAvailableDownloaders;
    private OnDateChangedListener dateChangedListener;

    private Provider<Downloaders> mDownloaders;
    private Spinner mPuzzleSelect;
    private ArrayAdapter<Downloader> mAdapter;
    private TextView mDateLabel;
    private int mDayOfMonth;
    private int mMonthOfYear;
    private int mYear;

    public DownloadPickerDialogBuilder(Activity a, final OnDownloadSelectedListener downloadButtonListener, int year,
        int monthOfYear, int dayOfMonth, Provider<Downloaders> provider) {
        mActivity = a;

        mYear = year;
        mMonthOfYear = monthOfYear;
        mDayOfMonth = dayOfMonth;

        mDownloaders = provider;

        LayoutInflater inflater = (LayoutInflater)mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ScrollView layout = (ScrollView)inflater.inflate(R.layout.download_dialog, (ViewGroup)mActivity.findViewById(R.id.download_root));

        mDateLabel = (TextView)layout.findViewById(R.id.dateLabel);
        updateDateLabel();

        dateChangedListener = new DatePicker.OnDateChangedListener() {
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                String curPuzzle = mPuzzleSelect.getSelectedItem().toString();
                mYear = year;
                mMonthOfYear = monthOfYear;
                mDayOfMonth = dayOfMonth;
                updateDateLabel();
                updatePuzzleSelect(curPuzzle);
            }
        };

        DatePicker datePicker = (DatePicker)layout.findViewById(R.id.datePicker);
        datePicker.init(year, monthOfYear, dayOfMonth, dateChangedListener);

        mPuzzleSelect = (Spinner)layout.findViewById(R.id.puzzleSelect);

        mAdapter = new ArrayAdapter<Downloader>(mActivity, android.R.layout.simple_spinner_item);
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mPuzzleSelect.setAdapter(mAdapter);

        updatePuzzleSelect(null);

        OnClickListener clickHandler = new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    downloadButtonListener.onDownloadSelected(getCurrentDate(), mAvailableDownloaders,
                        mPuzzleSelect.getSelectedItemPosition());
                }
            };

        AlertDialog.Builder builder = (new AlertDialog.Builder(mActivity)).setPositiveButton("Download", clickHandler)
                                       .setNegativeButton("Cancel", null)
                                       .setTitle("Download Puzzles");

        builder.setView(layout);
        mDialog = builder.create();
    }

    public Dialog getInstance() {
        return mDialog;
    }

    private Calendar getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(mYear, mMonthOfYear, mDayOfMonth);
        return calendar;
    }

    private void updateDateLabel() {
        mDateLabel.setText(DATE_FORMAT.format(getCurrentDate().getTime()));
    }

    public void updatePuzzleSelect(String toSelect) {
        mAvailableDownloaders = mDownloaders.get().getDownloaders(getCurrentDate());
        mAvailableDownloaders.add(0, new DummyDownloader(mActivity));

        mAdapter.setNotifyOnChange(false);
        mAdapter.clear();

        int indexToSelect = 0, i =0;
        for (Downloader downloader : mAvailableDownloaders) {
            mAdapter.add(downloader);
            if (downloader.toString().equals(toSelect)) {
                indexToSelect = i;
            }

            i++;
        }
        mAdapter.notifyDataSetChanged();

        mPuzzleSelect.setSelection(indexToSelect, false);
    }

    public interface OnDownloadSelectedListener {
        void onDownloadSelected(Calendar date, List<Downloader> availableDownloaders, int selected);
    }
}

