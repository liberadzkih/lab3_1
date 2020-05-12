package pl.com.bottega.ecommerce.sales.domain.productscatalog;

import java.util.List;

import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;

public interface ProductRepository {
	public Product load(Id productId);

	public List<Product> findProductWhereBestBeforeExpiredIn(int days);
}
