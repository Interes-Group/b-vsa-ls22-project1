package sk.stuba.fei.uim.vsa.pr1.tests;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sk.stuba.fei.uim.vsa.pr1.AbstractCarParkService;
import sk.stuba.fei.uim.vsa.pr1.TestData;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static sk.stuba.fei.uim.vsa.pr1.TestData.*;
import static sk.stuba.fei.uim.vsa.pr1.TestUtils.*;

class CarTest {

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
    void CAR01_shouldCreateCar() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        Object car = createNewCar();
        assertNotNull(car);
        testShouldHaveId(car);
        if (hasField(car, "user")) {
            Object carUser = getFieldValue(car, "user");
            assertNotNull(carUser);
            Object foundUser = carParkService.getUser(getFieldValue(carUser, "id", Long.class));
            assertNotNull(foundUser);
        }
    }

    private Object createNewCar() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        Object user = carParkService.createUser(TestData.User.firstName, TestData.User.lastName, TestData.User.email);
        return carParkService.createCar(getFieldValue(user, "id", Long.class),
                TestData.Car.brand, TestData.Car.model, TestData.Car.colour, TestData.Car.ecv);
    }

    @Test
    void CAR02_shouldGetCarById() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        Object car = createNewCar();
        Object found = carParkService.getCar(getFieldValue(car, "id", Long.class));
        assertNotNull(found);
        assertEquals(getFieldValue(car, "id"), getFieldValue(found, "id"));
    }

    @Test
    void CAR02_shouldGetCarByECV() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        Object car = createNewCar();
        Object found = carParkService.getCar(TestData.Car.ecv);
        assertNotNull(found);
        assertEquals(getFieldValue(car, "id"), getFieldValue(found, "id"));
    }

    @Test
    void CAR04_shouldGetCarsByUser() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        Object car = createNewCar();
        List<Object> users = carParkService.getUsers();
        assertEquals(1, users.size());
        List<Object> cars = carParkService.getCars(getFieldValue(users.get(0), "id", Long.class));
        assertNotNull(cars);
        assertEquals(1, cars.size());
        assertEquals(car.getClass(), cars.get(0).getClass());
        assertEquals(getFieldValue(car, "id"), getFieldValue(cars.get(0), "id"));
    }

    @Test
    void CAR05_shouldUpdateCarBrandAndModel() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        Object car = createNewCar();
        if (hasField(car, "brand") && hasField(car, "model")) {
            String newBrand = "Porsche";
            String newModel = "911 GTS";

            setFieldValue(car, "brand", newBrand);
            setFieldValue(car, "model", newModel);
            Object updated = carParkService.updateCar(car);
            assertNotNull(updated);
            assertEquals(newBrand, getFieldValue(updated, "brand", String.class));
            assertEquals(newModel, getFieldValue(updated, "model", String.class));
            assertEquals(getFieldValue(car, "id"), getFieldValue(updated, "id"));
        } else {
            fail("Car object does not have a brand and a model property. So this test cannot be performed!");
        }
    }

    @Test
    void CAR06_shouldDeleteCar() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        Object car = createNewCar();
        Object deleted = carParkService.deleteCar(getFieldValue(car, "id", Long.class));
        assertNotNull(deleted);
        Object notFound = carParkService.getCar(TestData.Car.ecv);
        assertNull(notFound);
    }

}