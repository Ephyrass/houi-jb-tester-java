package com.parkit.parkingsystem;

import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.service.InteractiveShell;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class InteractiveShellTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
    }

    @Test
    public void testExitOption() {
        try (MockedConstruction<InputReaderUtil> inputReaderUtilMock = mockConstruction(InputReaderUtil.class,
                (mock, context) -> when(mock.readSelection()).thenReturn(3));
             MockedConstruction<ParkingSpotDAO> parkingSpotDAOMock = mockConstruction(ParkingSpotDAO.class);
             MockedConstruction<TicketDAO> ticketDAOMock = mockConstruction(TicketDAO.class);
             MockedConstruction<ParkingService> parkingServiceMock = mockConstruction(ParkingService.class)) {

            InteractiveShell.loadInterface();

            String output = outContent.toString();
            assertTrue(output.contains("Welcome to Parking System!"));
            assertTrue(output.contains("Exiting from the system!"));

            ParkingService mockParkingService = parkingServiceMock.constructed().get(0);
            verify(mockParkingService, never()).processIncomingVehicle();
            verify(mockParkingService, never()).processExitingVehicle();
        }
    }

    @Test
    public void testIncomingVehicleOption() {
        try (MockedConstruction<InputReaderUtil> inputReaderUtilMock = mockConstruction(InputReaderUtil.class,
                (mock, context) -> when(mock.readSelection()).thenReturn(1).thenReturn(3));
             MockedConstruction<ParkingSpotDAO> parkingSpotDAOMock = mockConstruction(ParkingSpotDAO.class);
             MockedConstruction<TicketDAO> ticketDAOMock = mockConstruction(TicketDAO.class);
             MockedConstruction<ParkingService> parkingServiceMock = mockConstruction(ParkingService.class)) {

            InteractiveShell.loadInterface();

            ParkingService mockParkingService = parkingServiceMock.constructed().get(0);
            verify(mockParkingService, times(1)).processIncomingVehicle();
            verify(mockParkingService, never()).processExitingVehicle();
        }
    }

    @Test
    public void testExitingVehicleOption() {
        try (MockedConstruction<InputReaderUtil> inputReaderUtilMock = mockConstruction(InputReaderUtil.class,
                (mock, context) -> when(mock.readSelection()).thenReturn(2).thenReturn(3));
             MockedConstruction<ParkingSpotDAO> parkingSpotDAOMock = mockConstruction(ParkingSpotDAO.class);
             MockedConstruction<TicketDAO> ticketDAOMock = mockConstruction(TicketDAO.class);
             MockedConstruction<ParkingService> parkingServiceMock = mockConstruction(ParkingService.class)) {

            InteractiveShell.loadInterface();

            ParkingService mockParkingService = parkingServiceMock.constructed().get(0);
            verify(mockParkingService, never()).processIncomingVehicle();
            verify(mockParkingService, times(1)).processExitingVehicle();
        }
    }

    @Test
    public void testInvalidOption() {
        try (MockedConstruction<InputReaderUtil> inputReaderUtilMock = mockConstruction(InputReaderUtil.class,
                (mock, context) -> when(mock.readSelection()).thenReturn(5).thenReturn(3));
             MockedConstruction<ParkingSpotDAO> parkingSpotDAOMock = mockConstruction(ParkingSpotDAO.class);
             MockedConstruction<TicketDAO> ticketDAOMock = mockConstruction(TicketDAO.class);
             MockedConstruction<ParkingService> parkingServiceMock = mockConstruction(ParkingService.class)) {

            InteractiveShell.loadInterface();

            assertTrue(outContent.toString().contains("Unsupported option"));

            ParkingService mockParkingService = parkingServiceMock.constructed().get(0);
            verify(mockParkingService, never()).processIncomingVehicle();
            verify(mockParkingService, never()).processExitingVehicle();
        }
    }
}