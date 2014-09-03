/**
 * This file is part of Words With Crosses.
 *
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

import java.util.Calendar;

/**
 * American Values Club xword
 * URL: http://www.avxword.com/
 * Date: Wednesday
 */
public class AVXWDownloader extends XWordHubDownloader
{
    private static final Calendar START_DATE;

    static
    {
        Calendar startDate = Calendar.getInstance();
        startDate.clear();
        startDate.set(2013, 0, 2);  // 2013-01-02 (months start at 0 for Calendar!)
        START_DATE = startDate;
    }

    public AVXWDownloader(String username, String password)
    {
        super("American Values Club xword", "avclub", username, password);
    }

    public boolean isPuzzleAvailable(Calendar date)
    {
        return
            (date.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY) &&
            (date.compareTo(START_DATE) >= 0);
    }
}
