package sk.stuba.fei.uim.vsa.pr1.tests;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sk.stuba.fei.uim.vsa.pr1.AbstractCarParkService;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static sk.stuba.fei.uim.vsa.pr1.TestData.*;
import static sk.stuba.fei.uim.vsa.pr1.TestUtils.*;

class ParkingSpotTest {

    private static AbstractCarParkService carParkService;
    private static Connection mysql;

    @BeforeAll
    static void setup() throws SQLException, ClassNotFoundException {
        carParkService = getServiceClass();
        mysql = getMySQL(DB, USERNAME, PASSWORD);
    }

    @BeforeEach
    void beforeEach() {
        clearDB(mysql);
    }

    @Test
    void SPOT01_getAllParkingSlotsWithoutType() throws Exception {
        Object carPark = carParkService.createCarPark("test8", "testtest", 12);
        assertNotNull(carPark);
        testShouldHaveId(carPark);
        Long carParkId = getEntityId(carPark);

        Object floor1 = carParkService.createCarParkFloor(carParkId, "Floor3-1");
        assertNotNull(floor1);

        Object spot1 = carParkService.createParkingSpot(carParkId, "Floor3-1", "1.01");
        assertNotNull(spot1);
        testShouldHaveId(spot1);
        Long spot1Id = getEntityId(spot1);
        Object spot1Loaded = carParkService.getParkingSpot(spot1Id);
        assertNotNull(spot1Loaded);
        Long spot1LoadedId = getEntityId(spot1Loaded);
        String[] fields = findFieldByNameAndType(spot1, "identifier", String.class);
        assertNotNull(fields);
        assertTrue(fields.length > 0);
        String identifierField = fields[0];
        String spot1Identifier = getFieldValue(spot1, identifierField, String.class);
        String spot1LoadedIdentifier = getFieldValue(spot1Loaded, identifierField, String.class);
        assertEquals(spot1Identifier, spot1LoadedIdentifier);
        assertEquals(spot1Id, spot1LoadedId);

        Object spot2 = carParkService.createParkingSpot(carParkId, "Floor3-1", "1.02");
        assertNotNull(spot2);
        testShouldHaveId(spot2);
        Long spot2Id = getEntityId(spot2);
        Object spot2Loaded = carParkService.getParkingSpot(spot2Id);
        assertNotNull(spot2Loaded);
        assertEquals(spot2Id, getEntityId(spot2Loaded));

        Object floor2 = carParkService.createCarParkFloor(carParkId, "Floor3-2");
        assertNotNull(floor2);
        Object spot21 = carParkService.createParkingSpot(carParkId, "Floor3-2", "2.01");
        assertNotNull(spot21);
        testShouldHaveId(spot21);
        Long spot21Id = getEntityId(spot21);
        Object spot21Loaded = carParkService.getParkingSpot(spot21Id);
        assertNotNull(spot21Loaded);
        assertEquals(spot21Id, getEntityId(spot21Loaded));


        Map<String, List<Object>> floors = carParkService.getParkingSpots(carParkId);
        assertNotNull(floors);
        assertEquals(2, floors.size());
        List<Object> firstFloorSpots = floors.get("Floor3-1");
        assertNotNull(firstFloorSpots);
        assertEquals(2, firstFloorSpots.size());
        testShouldHaveId(firstFloorSpots.get(0));
        testShouldHaveId(firstFloorSpots.get(1));
        assertNotEquals(getEntityId(firstFloorSpots.get(0)), getEntityId(firstFloorSpots.get(1)));
        assertTrue(firstFloorSpots.stream().anyMatch(s -> {
            try {
                return Objects.equals(getEntityId(firstFloorSpots.get(0)), spot1Id);
            } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                e.printStackTrace();
                return false;
            }
        }));
        List<Object> secondFloorSpots = floors.get("Floor3-2");
        assertNotNull(secondFloorSpots);
        assertEquals(1, secondFloorSpots.size());
        Object secondFloorSpot = secondFloorSpots.get(0);
        testShouldHaveId(secondFloorSpot);
        assertEquals(spot21Id, getEntityId(secondFloorSpot));
    }


    @Test
    void SPOT02_createAndGetParkingSpotWithoutType() {
        try {
            Object carPark = carParkService.createCarPark("test7", "testtest", 12);
            assertNotNull(carPark);
            Class c = carPark.getClass();
            Method[] methods = c.getMethods();
            Method getId = null;
            for (Method m : methods) {
                if (m.getReturnType() == Long.class) {
                    getId = m;
                    break;
                }
            }
            assertNotNull(getId);
            Long id = (Long) getId.invoke(carPark);
            assertNotNull(id);

            Object carParkFloor = carParkService.createCarParkFloor(id, "Floor3-1");
            assertNotNull(carParkFloor);

            Object spot1 = carParkService.createParkingSpot(id, "Floor3-1", "1.01");
            assertNotNull(spot1);

            Method getParkingSpotId = null;
            Method getParkingSpotIdentifier = null;
            Class parkingSpotClass = spot1.getClass();

            for (Method m : parkingSpotClass.getMethods()) {
                if (m.getParameterCount() == 0) {
                    if (m.getReturnType() == Long.class) {
                        getParkingSpotId = m;
                    } else if (m.getReturnType() == String.class && !m.getName().equals("toString")) {
                        getParkingSpotIdentifier = m;
                    }
                }
            }

            assertNotNull(getParkingSpotId);
            assertNotNull(getParkingSpotIdentifier);

            Long spotId = (Long) getParkingSpotId.invoke(spot1);
            assertNotNull(spotId);

            Object carParkSpot = carParkService.getParkingSpot(spotId);
            assertNotNull(carParkSpot);

            Long carParkSpotId = (Long) getParkingSpotId.invoke(carParkSpot);
            assertNotNull(carParkSpotId);

            String spotIdentifier = (String) getParkingSpotIdentifier.invoke(spot1);
            String parkingSpotIdentifier = (String) getParkingSpotIdentifier.invoke(carParkSpot);

            assertNotNull(spotIdentifier);
            assertNotNull(parkingSpotIdentifier);

            assertEquals(spotIdentifier, parkingSpotIdentifier);


        } catch (Exception e) {
            fail();
        }
    }

    @Test
    void SPOT03_getAllParkingSpotsForCarParkWithoutType() throws Exception {
        Object carPark = carParkService.createCarPark("test7", "testtest", 12);
        assertNotNull(carPark);

        Long carParkId = getEntityId(carPark);
        assertNotNull(carParkId);

        Object floor1 = carParkService.createCarParkFloor(carParkId, "Floor1");
        Object floor2 = carParkService.createCarParkFloor(carParkId, "Floor2");
        assertNotNull(floor1);
        assertNotNull(floor2);

        Object floor1Spot1 = carParkService.createParkingSpot(carParkId, "Floor1", "1.1");
        Object floor1Spot2 = carParkService.createParkingSpot(carParkId, "Floor1", "1.2");

        Object floor2Spot1 = carParkService.createParkingSpot(carParkId, "Floor2", "2.1");
        Object floor2Spot2 = carParkService.createParkingSpot(carParkId, "Floor2", "2.2");

        assertNotNull(floor1Spot1);
        assertNotNull(floor1Spot2);
        assertNotNull(floor2Spot1);
        assertNotNull(floor2Spot2);

        Map<String, List<Object>> map = carParkService.getParkingSpots(carParkId);
        assertNotNull(map);
        assertEquals(2, map.keySet().size());
        assertTrue(map.containsKey("Floor1"));
        assertTrue(map.containsKey("Floor2"));

        List<Object> floor1Slots = map.get("Floor1");
        List<Object> floor2Slots = map.get("Floor2");
        assertNotNull(floor1Slots);
        assertNotNull(floor2Slots);

        assertEquals(2, floor1Slots.size());
        assertEquals(2, floor2Slots.size());

        Long floor1Spot1Id = getEntityId(floor1Spot1);
        Long floor1Spot2Id = getEntityId(floor1Spot2);
        Long floor2Spot1Id = getEntityId(floor2Spot1);
        Long floor2Spot2Id = getEntityId(floor2Spot2);

        assertNotNull(floor1Spot1Id);
        assertNotNull(floor1Spot2Id);
        assertNotNull(floor2Spot1Id);
        assertNotNull(floor2Spot2Id);

        Object fl1Spot1 = floor1Slots.get(0);
        Object fl1Spot2 = floor1Slots.get(1);

        Object fl2Spot1 = floor2Slots.get(0);
        Object fl2Spot2 = floor2Slots.get(1);

        assertNotNull(fl1Spot1);
        assertNotNull(fl1Spot2);
        assertNotNull(fl2Spot1);
        assertNotNull(fl2Spot2);

        String[] spotFields = findFieldByType(floor1Spot1, String.class);
        assertNotNull(spotFields);

        Long fl1Spot1Id = getEntityId(fl1Spot1);
        Long fl1Spot2Id = getEntityId(fl1Spot2);
        Long flr2Spot1Id = getEntityId(fl2Spot1);
        Long flr2Spot2Id = getEntityId(fl2Spot2);

        assertNotNull(fl1Spot1Id);
        assertNotNull(fl1Spot2Id);
        assertNotNull(flr2Spot1Id);
        assertNotNull(flr2Spot2Id);

        if (floor1Spot1Id.equals(fl1Spot1Id)) {
            if (floor1Spot2Id.equals(fl1Spot2Id)) {
                for (String f : spotFields) {
                    assertEquals(getFieldValue(floor1Spot1, f, String.class), getFieldValue(fl1Spot1, f, String.class));
                    assertEquals(getFieldValue(floor1Spot2, f, String.class), getFieldValue(fl1Spot2, f, String.class));

                }
            } else {
                fail();
            }

        } else if (floor1Spot2Id.equals(fl1Spot2Id)) {
            for (String f : spotFields) {
                assertEquals(getFieldValue(floor1Spot2, f, String.class), getFieldValue(fl1Spot1, f, String.class));
                assertEquals(getFieldValue(floor1Spot1, f, String.class), getFieldValue(fl1Spot2, f, String.class));
            }

        } else {
            fail();
        }

        if (floor2Spot1Id.equals(flr2Spot1Id)) {
            if (floor2Spot2Id.equals(flr2Spot2Id)) {
                for (String f : spotFields) {
                    assertEquals(getFieldValue(floor2Spot1, f, String.class), getFieldValue(fl2Spot1, f, String.class));
                    assertEquals(getFieldValue(floor2Spot2, f, String.class), getFieldValue(fl2Spot2, f, String.class));
                }
            } else {
                fail();
            }
        } else if (floor2Spot2Id.equals(flr2Spot1Id)) {
            if (floor2Spot1Id.equals(flr2Spot2Id)) {
                for (String f : spotFields) {
                    assertEquals(getFieldValue(floor2Spot2, f, String.class), getFieldValue(fl2Spot1, f, String.class));
                    assertEquals(getFieldValue(floor2Spot1, f, String.class), getFieldValue(fl2Spot2, f, String.class));
                }
            } else {
                fail();
            }
        } else {
            fail();
        }
    }

    @Test
    void SPOT05_deleteParkingSpotWithoutType() throws Exception {
        Object carPark = carParkService.createCarPark("test7", "testtest", 12);
        assertNotNull(carPark);

        Long carParkId = getEntityId(carPark);
        assertNotNull(carParkId);

        Object floor1 = carParkService.createCarParkFloor(carParkId, "Floor1");
        assertNotNull(floor1);

        Object floor1Spot1 = carParkService.createParkingSpot(carParkId, "Floor1", "1.1");
        assertNotNull(floor1Spot1);

        Long floor1Spot1Id = getEntityId(floor1Spot1);
        assertNotNull(floor1Spot1Id);

        Object fl1Spot1 = carParkService.getParkingSpot(floor1Spot1Id);
        assertNotNull(fl1Spot1);

        Object deletedSpot = carParkService.deleteParkingSpot(floor1Spot1Id);

        try {
            Object delSpot = carParkService.getParkingSpot(floor1Spot1Id);
            assertNull(delSpot);
        } catch (Exception e) {
            assertTrue(true);
        }
    }

    @Test
    void SPOT06_uniqueNameForParkingSpotWithoutType() throws Exception {
        Object carPark = carParkService.createCarPark("test7", "testtest", 12);
        assertNotNull(carPark);

        Long carParkId = getEntityId(carPark);
        assertNotNull(carParkId);

        Object floor1 = carParkService.createCarParkFloor(carParkId, "Floor1");
        assertNotNull(floor1);

        Object floor1Spot1 = carParkService.createParkingSpot(carParkId, "Floor1", "1.1");
        assertNotNull(floor1Spot1);

        Long floor1Spot1Id = getEntityId(floor1Spot1);
        assertNotNull(floor1Spot1Id);

        Object fl1Spot1 = carParkService.getParkingSpot(floor1Spot1Id);
        assertNotNull(fl1Spot1);

        Object carPark2 = carParkService.createCarPark("test8", "testtest", 12);
        assertNotNull(carPark);

        Long carPark2Id = getEntityId(carPark2);
        assertNotNull(carPark2Id);

        Object floor2 = carParkService.createCarParkFloor(carPark2Id, "Floor2");
        assertNotNull(floor2);

        Object floor2Spot1 = carParkService.createParkingSpot(carPark2Id, "Floor2", "1.1");
        assertNotNull(floor2Spot1);

        Long floor2Spot1Id = getEntityId(floor2Spot1);
        assertNotNull(floor1Spot1Id);

        Object fl2Spot1 = carParkService.getParkingSpot(floor2Spot1Id);
        assertNotNull(fl2Spot1);

        try {
            Object floor1Spot2 = carParkService.createParkingSpot(carParkId, "Floor1", "1.1");
            assertNull(floor1Spot2);
        } catch (Exception e) {
            assertTrue(true);
        }

    }

    @Test
    public void SPOT07_getAllParkingSpotForFloorWithoutType() throws Exception {
        Object carPark = carParkService.createCarPark("test8", "testtest", 12);
        assertNotNull(carPark);
        testShouldHaveId(carPark);
        Long carParkId = getEntityId(carPark);

        Object floor1 = carParkService.createCarParkFloor(carParkId, "Floor3-1");
        assertNotNull(floor1);

        Object spot1 = carParkService.createParkingSpot(carParkId, "Floor3-1", "1.01");
        assertNotNull(spot1);
        testShouldHaveId(spot1);
        Long spot1Id = getEntityId(spot1);

        Object spot2 = carParkService.createParkingSpot(carParkId, "Floor3-1", "1.02");
        assertNotNull(spot2);
        testShouldHaveId(spot2);
        Long spot2Id = getEntityId(spot2);

        String[] fields = findFieldByType(spot1, String.class);

        List<Object> floors = carParkService.getParkingSpots(carParkId, "Floor3-1");
        assertNotNull(floors);
        assertEquals(2, floors.size());

        Object sp1 = floors.get(0);
        Object sp2 = floors.get(1);
        assertNotNull(sp1);
        assertNotNull(sp2);
        testShouldHaveId(sp1);
        testShouldHaveId(sp2);

        Long sp1Id = getEntityId(sp1);
        Long sp2Id = getEntityId(sp2);

        if (Objects.equals(spot1Id, sp1Id)) {
            if (Objects.equals(spot2Id, sp2Id)) {
                for (String f : fields) {
                    assertEquals(getFieldValue(spot1, f, String.class), getFieldValue(sp1, f, String.class));
                    assertEquals(getFieldValue(spot2, f, String.class), getFieldValue(sp2, f, String.class));
                }
            } else {
                fail();
            }
        } else if (Objects.equals(spot2Id, sp1Id)) {
            if (Objects.equals(spot1Id, sp2Id)) {
                for (String f : fields) {
                    assertEquals(getFieldValue(spot2, f, String.class), getFieldValue(sp1, f, String.class));
                    assertEquals(getFieldValue(spot1, f, String.class), getFieldValue(sp2, f, String.class));
                }
            }
        } else {
            fail();
        }
    }
}
