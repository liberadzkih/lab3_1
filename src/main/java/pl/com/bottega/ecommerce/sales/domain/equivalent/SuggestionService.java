package pl.com.bottega.ecommerce.sales.domain.equivalent;

import pl.com.bottega.ecommerce.sales.domain.client.Client;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.Product;

public interface SuggestionService {
	public Product suggestEquivalent(Product problematicProduct, Client client);
}
