package pl.com.bottega.ecommerce.sales.domain.client;

import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;

public interface ClientRepository {
	public Client load(Id id);

	public void save(Client client);
}
