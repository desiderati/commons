/*
 * Copyright (c) 2020 - Felipe Desiderati
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.herd.common.google;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import io.herd.common.configuration.annotation.CustomSpringBootApplication;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;

@Slf4j
@CustomSpringBootApplication
public class GoogleCalendarServiceTestApplication {

    public static void main(String[] args) throws IOException {
        ConfigurableApplicationContext context =
            new SpringApplicationBuilder(GoogleCalendarServiceTestApplication.class).web(WebApplicationType.NONE).run(args);
        GoogleCalendarService googleCalendarService = context.getBean(GoogleCalendarService.class);

        // List all calendars associated with the account.
        Calendar calendar = googleCalendarService.getCalendar();
        String pageToken = null;
        do {
            CalendarList calendarList = calendar.calendarList()
                .list()
                .setPageToken(pageToken)
                .execute();

            for (CalendarListEntry listEntry : calendarList.getItems()) {
                log.info(listEntry.getId());
            }
            pageToken = calendarList.getNextPageToken();

        } while (pageToken != null);
    }
}
