package pl.com.bottega.ecommerce.sales.domain.reservation;

import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;

public interface ReservationRepository {
	void save(Reservation reservation);

	Reservation load(Id reservationId);
}
