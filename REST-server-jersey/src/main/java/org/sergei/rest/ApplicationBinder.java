package org.sergei.rest;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.sergei.rest.dao.AircraftDAO;
import org.sergei.rest.dao.CustomerDAO;
import org.sergei.rest.service.AircraftService;
import org.sergei.rest.service.CustomerService;

public class ApplicationBinder extends AbstractBinder {
    @Override
    protected void configure() {
        bind(CustomerService.class).to(CustomerService.class);
        bind(CustomerDAO.class).to(CustomerDAO.class);
        bind(AircraftService.class).to(AircraftService.class);
        bind(AircraftDAO.class).to(AircraftDAO.class);
    }
}
