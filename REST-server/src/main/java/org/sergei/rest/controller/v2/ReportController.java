package org.sergei.rest.controller.v2;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.sergei.rest.model.CustomerReport;
import org.sergei.rest.repository.CustomerReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.sergei.rest.controller.util.LinkUtil.setLinksForReport;

/**
 * @author Sergei Visotsky
 */
@Api(
        value = "/api/v2/reports",
        produces = "application/json",
        consumes = "application/json"
)
@RestController
@RequestMapping(value = "/api/v2/reports", produces = "application/json")
public class ReportController {

    @Autowired
    private CustomerReportRepository customerReportRepository;

    @ApiOperation("Get report for customer")
    @GetMapping(value = "/customers", params = "customerId")
    public ResponseEntity<Resources> getReportForCustomer(@RequestParam Long customerId) {
        List<CustomerReport> customerReport = customerReportRepository.findByCustomerId(customerId);
        return new ResponseEntity<>(setLinksForReport(customerId, customerReport), HttpStatus.OK);
    }

    @ApiOperation("Get paginated report for customer")
    @GetMapping(value = "/customers", params = {"customerId", "page", "size"})
    public ResponseEntity<Resources> getPaginatedReportForCustomer(@ApiParam("Customer ID to find report")
                                                                   @RequestParam("customerId") Long customerId,
                                                                   @ApiParam("Number of page")
                                                                   @RequestParam("page") int page,
                                                                   @ApiParam("Number of elements per page")
                                                                   @RequestParam("size") int size) {
        Page<CustomerReport> customerReport =
                customerReportRepository.findPaginatedByCustomerId(customerId, PageRequest.of(page, size));
        return new ResponseEntity<>(setLinksForReport(customerId, customerReport), HttpStatus.OK);
    }
}