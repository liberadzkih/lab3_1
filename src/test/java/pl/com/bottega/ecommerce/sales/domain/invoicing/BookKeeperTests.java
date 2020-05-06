package pl.com.bottega.ecommerce.sales.domain.invoicing;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.ClientData;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductType;
import pl.com.bottega.ecommerce.sharedkernel.Money;

public class BookKeeperTests {
	private BookKeeper bookKeeper;
	private InvoiceRequest invoiceRequest;
	private TaxPolicy taxPolicy;
	
	@Before
	public void init() {
		bookKeeper = new BookKeeper(new InvoiceFactory());
		invoiceRequest = new InvoiceRequest(new ClientData(Id.generate(), "name"));
		taxPolicy = Mockito.mock(TaxPolicy.class);
		
		Mockito.when(taxPolicy.calculateTax(Mockito.any(ProductType.class), Mockito.any(Money.class)))
			.thenReturn(new Tax(new Money(0.0), "description"));
	}
	
	@Test
	public void invoiceWithOneElement() {
		invoiceRequest.add(new RequestItemBuilder().build());
		Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicy);
		assertEquals(1, invoice.getItems().size());
	}
	
	@Test
	public void invoiceWithoutElements() {
		Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicy);
		assertEquals(0, invoice.getItems().size());
	}
	
	@Test
	public void invoiceWithSpecificElementsSum() {
		invoiceRequest.add(new RequestItemBuilder()
				.withTotalCost(new Money(10.0))
				.build());
		
		invoiceRequest.add(new RequestItemBuilder()
				.withTotalCost(new Money(20.0))
				.build());

		Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicy);
		assertEquals(new Money(30.0), invoice.getNet());
	}
	
	@Test
	public void calculateTaxCalledTwoTimes() {
		invoiceRequest.add(new RequestItemBuilder()
				.withProductData(new ProductBuilder().withProductType(ProductType.DRUG).build().generateSnapshot())
				.withTotalCost(new Money(10.0))
				.build());
		
		invoiceRequest.add(new RequestItemBuilder()
				.withProductData(new ProductBuilder().withProductType(ProductType.FOOD).build().generateSnapshot())
				.withTotalCost(new Money(20.0))
				.build());
		
		bookKeeper.issuance(invoiceRequest, taxPolicy);
		
		Mockito.verify(taxPolicy).calculateTax(ProductType.DRUG, new Money(10.0));
		Mockito.verify(taxPolicy).calculateTax(ProductType.FOOD, new Money(20.0));
		Mockito.verify(taxPolicy, Mockito.times(2)).calculateTax(Mockito.any(ProductType.class), Mockito.any(Money.class));
	}
	
	@Test
	public void calculateTaxCalledOneTimes() {
		invoiceRequest.add(new RequestItemBuilder()
				.withProductData(new ProductBuilder().withProductType(ProductType.DRUG).build().generateSnapshot())
				.withTotalCost(new Money(10.0))
				.build());
		
		bookKeeper.issuance(invoiceRequest, taxPolicy);
		
		Mockito.verify(taxPolicy).calculateTax(ProductType.DRUG, new Money(10.0));
		Mockito.verify(taxPolicy, Mockito.times(1)).calculateTax(Mockito.any(ProductType.class), Mockito.any(Money.class));
	}
	
	@Test
	public void calculateTaxCalledZeroTimes() {
		bookKeeper.issuance(invoiceRequest, taxPolicy);
		
		Mockito.verify(taxPolicy, Mockito.times(0)).calculateTax(Mockito.any(ProductType.class), Mockito.any(Money.class));
	}
}
