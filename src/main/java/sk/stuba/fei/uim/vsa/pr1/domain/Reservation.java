package sk.stuba.fei.uim.vsa.pr1.domain;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
public class Reservation implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private LocalDateTime startsAt;


    private LocalDateTime endsAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    private Double price = null;

    /**
     * Get the value of price
     *
     * @return the value of price
     */
    public Double getPrice() {
        return price;
    }

    /**
     * Set the value of price
     *
     * @param price new value of price
     */
    public void setPrice(Double price) {
        this.price = price;
    }


    @ManyToOne
    private ParkingSpot parkingSpot;

    @ManyToOne
    private Car car;

    public ParkingSpot getParkingSpot() {
        return parkingSpot;
    }

    public void setParkingSpot(ParkingSpot parkingSpot) {
        this.parkingSpot = parkingSpot;
    }

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }

    /**
     * Get the value of endsAt
     *
     * @return the value of endsAt
     */
    public LocalDateTime getEndsAt() {
        return endsAt;
    }

    /**
     * Set the value of endsAt
     *
     * @param endsAt new value of endsAt
     */
    public void setEndsAt(LocalDateTime endsAt) {
        this.endsAt = endsAt;
    }

    /**
     * Get the value of start
     *
     * @return the value of start
     */
    public LocalDateTime getStartsAt() {
        return startsAt;
    }

    /**
     * Set the value of start
     *
     * @param start new value of start
     */
    public void setStartsAt(LocalDateTime startsAt) {
        this.startsAt = startsAt;
    }

    public void endReservation() {
        LocalDateTime now = LocalDateTime.now();
        int pricePerHour = this.parkingSpot.getCarParkFloor().getCarPark().getPricePerHour();

        this.endsAt = now;
        long diff = ChronoUnit.HOURS.between(this.startsAt, this.endsAt);
        long minDiff = ChronoUnit.MINUTES.between(this.startsAt, this.endsAt);
        if (minDiff > 0) {
            diff++;
        }

        this.price = new Long(diff).doubleValue() * pricePerHour;
    }

    public void endReservation(long holidayHours) {
        LocalDateTime now = LocalDateTime.now();
        int pricePerHour = this.parkingSpot.getCarParkFloor().getCarPark().getPricePerHour();

        this.endsAt = now;
        long diff = ChronoUnit.HOURS.between(this.startsAt, this.endsAt);
        long minDiff = ChronoUnit.MINUTES.between(this.startsAt, this.endsAt);
        if (minDiff > 0) {
            diff++;
        }

        this.price = new Long(diff).doubleValue() * pricePerHour;
        Double holidayPrice = (new Long(holidayHours).doubleValue() * pricePerHour * 0.25);
        this.price -= holidayPrice;
    }

    public void endReservation(Integer discount) {
        endReservation();
        this.price = this.price * (1 - (discount.doubleValue() / 100.0));
    }
}
