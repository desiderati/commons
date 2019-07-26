/*
 * Copyright (c) 2019 - Felipe Desiderati ALL RIGHTS RESERVED.
 *
 * This software is protected by international copyright laws and cannot be
 * used, copied, stored or distributed without prior authorization.
 */
package br.tech.desiderati.common.google;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import br.tech.desiderati.common.configuration.annotation.CustomSpringBootApplication;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;

@Slf4j
@CustomSpringBootApplication
public class GoogleCalendarServiceTestApplication {

    public static void main(String[] args) throws IOException {
        // Para aplicações Non-Web precisamos inicializar a aplicação desta forma.
        // Evitando assim que o Container Web seja carregado desnecessariamente.
        // Por padrão, todas as aplicações possuem dependência com o Javax Servlet.
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
