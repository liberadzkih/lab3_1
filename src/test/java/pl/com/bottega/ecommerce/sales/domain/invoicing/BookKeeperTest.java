package pl.com.bottega.ecommerce.sales.domain.invoicing;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.ClientData;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.Product;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductType;
import pl.com.bottega.ecommerce.sharedkernel.Money;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

class BookKeeperTest {

    private BookKeeper bookKeeper = new BookKeeper(new InvoiceFactory());
    private InvoiceRequest invoiceRequest;

    @Mock
    TaxPolicy taxMock;

    @BeforeEach
    void setUp() {

        invoiceRequest = new InvoiceRequest(new ClientData(Id.generate(), "ABC"));

        taxMock = Mockito.mock(TaxPolicy.class);
        Mockito.when(taxMock.calculateTax(any(ProductType.class), any(Money.class)))
                .thenReturn(new Tax(new Money(0.23), "23%"));
    }

    @Test
    public void invoiceRequestWithOnePositionShouldReturnInvoiceWithOnePositionTest() {

        Product product = new ProductBuilder().build();
        RequestItem requestItem = new RequestItemBuilder().withProductData(product.generateSnapshot()).build();

        invoiceRequest.add(requestItem);

        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxMock);

        Assertions.assertEquals(1, invoice.getItems().size());

    }

    @Test
    public void invoiceRequestWithTwoPositionsShouldCallCalculateTaxTwoTimesTest() {

        Product product1 = new ProductBuilder().withName("Welchol").withProductType(ProductType.DRUG).build();
        Product product2 = new ProductBuilder().withName("Apple").withProductType(ProductType.FOOD).build();

        RequestItem requestItem1 = new RequestItemBuilder().withProductData(product1.generateSnapshot())
                .withTotalCost(new Money(BigDecimal.valueOf(100)))
                .build();
        invoiceRequest.add(requestItem1);

        RequestItem requestItem2 = new RequestItemBuilder().withProductData(product2.generateSnapshot())
                .withTotalCost(new Money(BigDecimal.valueOf(100)))
                .build();
        invoiceRequest.add(requestItem2);

        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxMock);

        Mockito.verify(taxMock, times(1)).calculateTax(ProductType.DRUG, requestItem1.getTotalCost());
        Mockito.verify(taxMock, times(1)).calculateTax(ProductType.FOOD, requestItem2.getTotalCost());
    }

    @Test
    public void invoiceRequestWithNoPositionsShouldReturnInvoiceWithZeroPositionsTest() {

        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxMock);

        Assertions.assertEquals(0, invoice.getItems().size());

    }

    @Test
    public void invoiceGetItemsShouldReturnProperInformation() {

        Product product1 = new ProductBuilder().withName("Welchol").withProductType(ProductType.DRUG).build();
        Product product2 = new ProductBuilder().withName("Apple").withProductType(ProductType.FOOD).build();

        RequestItem requestItem = new RequestItemBuilder().withProductData(product1.generateSnapshot())
                .withTotalCost(new Money(BigDecimal.valueOf(100)))
                .build();
        RequestItem requestItem2 = new RequestItemBuilder().withProductData(product2.generateSnapshot())
                .withTotalCost(new Money(BigDecimal.valueOf(100)))
                .build();
        invoiceRequest.add(requestItem);
        invoiceRequest.add(requestItem2);

        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxMock);

        List<String> productList = new ArrayList<>();
        productList.add("Welchol");
        productList.add("Apple");

        List<ProductType> productTypeList = new ArrayList<>();
        productTypeList.add(ProductType.DRUG);
        productTypeList.add(ProductType.FOOD);

        for (int i = 0; i < productList.size(); i++) {
            Assertions.assertEquals(productList.get(i), invoice.getItems().get(i).getProduct().getName());
            Assertions.assertEquals(productTypeList.get(i), invoice.getItems().get(i).getProduct().getType());
        }

    }

    @Test
    public void invoiceRequestShouldCallRequestItemGetTotalCostTwoTimesTest() {

        Product product = new ProductBuilder().build();
        RequestItem requestItem1 = mock(RequestItem.class);
        when(requestItem1.getTotalCost()).thenReturn(new Money(0));
        when(requestItem1.getProductData()).thenReturn(product.generateSnapshot());
        when(requestItem1.getQuantity()).thenReturn(0);
        invoiceRequest.add(requestItem1);
        invoiceRequest.add(requestItem1);

        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxMock);

        verify(taxMock, times(2)).calculateTax(any(ProductType.class), any(Money.class));
    }

}