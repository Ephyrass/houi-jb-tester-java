package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.model.ParkingSpot;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static final DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    public static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    public void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    public static void tearDown(){

    }

    @Test
    public void testParkingACar(){
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();

        // Check that a ticket is actually saved in DB and Parking table is updated with availability
        String vehicleRegNumber = "ABCDEF";
        Ticket ticket = ticketDAO.getTicket(vehicleRegNumber);

        assertNotNull(ticket, "Ticket should not be null");
        assertEquals(vehicleRegNumber, ticket.getVehicleRegNumber(), "Registration number should match");
        assertNotNull(ticket.getInTime(), "In time should be populated");
        assertNull(ticket.getOutTime(), "Out time should be null");

        // Verify that parking spot is marked as occupied
        ParkingSpot parkingSpot = ticket.getParkingSpot();
        assertFalse(parkingSpot.isAvailable(), "Parking spot should be marked as unavailable");
    }

    @Test
    public void testParkingLotExit(){
        testParkingACar();
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processExitingVehicle();

        // Check that the fare generated and out time are populated correctly in the database
        String vehicleRegNumber = "ABCDEF";
        Ticket ticket = ticketDAO.getTicket(vehicleRegNumber);

        assertNotNull(ticket, "Ticket should not be null");
        assertNotNull(ticket.getOutTime(), "Out time should be populated");
        assertTrue(ticket.getPrice() >= 0, "Price should be set and positive");

    }

    @Test
    public void testParkingLotExitRecurringUser() throws Exception {
        // First parking
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();

        // Wait a bit to simulate parking duration
        Thread.sleep(100);

        // First exit
        parkingService.processExitingVehicle();
        Ticket firstTicket = ticketDAO.getTicket("ABCDEF");
        double firstPrice = firstTicket.getPrice();

        dataBasePrepareService.clearDataBaseEntries();

        // Second parking (same vehicle)
        parkingService.processIncomingVehicle();

        // Wait the same duration
        Thread.sleep(100);

        // Second exit
        parkingService.processExitingVehicle();
        Ticket secondTicket = ticketDAO.getTicket("ABCDEF");
        double secondPrice = secondTicket.getPrice();

        double expectedDiscountedPrice = firstPrice * 0.95;

        assertEquals(expectedDiscountedPrice, secondPrice, 0.1,
                "For recurring users, the price should be 5% less than the regular price");
    }
}
