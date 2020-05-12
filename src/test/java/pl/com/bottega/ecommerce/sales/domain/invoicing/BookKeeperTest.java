package pl.com.bottega.ecommerce.sales.domain.invoicing;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.ClientData;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductData;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductType;
import pl.com.bottega.ecommerce.sharedkernel.Money;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

class BookKeeperTest {

    private BookKeeper bookKeeper;
    private InvoiceRequest invoiceRequest;
    private ClientData clientData;
    private ProductData productData;

    @Mock
    TaxPolicy taxMock;

    @BeforeEach
    void setup() {

        bookKeeper = new BookKeeper(new InvoiceFactory());
        clientData = new ClientData(Id.generate(), "client");
        invoiceRequest = new InvoiceRequest(clientData);
    }

    @Test
    public void invoiceRequestWithOnePositionShouldReturnInvoiceWithOnePositionTest() {

        taxMock = mock(TaxPolicy.class);
        when(taxMock.calculateTax(ProductType.STANDARD, new Money(3))).thenReturn(new Tax(new Money(0.23), "23%"));

        productData = mock(ProductData.class);
        when(productData.getType()).thenReturn(ProductType.STANDARD);

        RequestItem requestItem = new RequestItem(productData, 1, new Money(3));
        invoiceRequest.add(requestItem);

        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxMock);

        Assertions.assertEquals(1, invoice.getItems().size());

    }

    @Test
    public void invoiceRequestWithTwoPositionsShouldCallCalculateTaxTwoTimesTest() {

        Money money1 = new Money(3);
        Money money2 = new Money(5);

        TaxPolicy taxPolicy1 = mock(TaxPolicy.class);
        when(taxPolicy1.calculateTax(ProductType.STANDARD, money1)).thenReturn(new Tax(new Money(0.23), "23%"));
        when(taxPolicy1.calculateTax(ProductType.FOOD, money2)).thenReturn(new Tax(new Money(0.46), "46%"));

        ProductData productData1 = mock(ProductData.class);
        when(productData1.getType()).thenReturn(ProductType.STANDARD);

        ProductData productData2 = mock(ProductData.class);
        when(productData2.getType()).thenReturn(ProductType.FOOD);

        RequestItem requestItem1 = new RequestItem(productData1, 5, new Money(3));
        invoiceRequest.add(requestItem1);

        RequestItem requestItem2 = new RequestItem(productData2, 2, new Money(5));
        invoiceRequest.add(requestItem2);

        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicy1);

        Mockito.verify(taxPolicy1, times(1)).calculateTax(ProductType.STANDARD, money1);
        Mockito.verify(taxPolicy1, times(1)).calculateTax(ProductType.FOOD, money2);
    }

    @Test
    public void invoiceRequestWithNoPositionsShouldReturnInvoiceWithZeroPositionsTest() {

        taxMock = mock(TaxPolicy.class);
        when(taxMock.calculateTax(ProductType.STANDARD, new Money(3))).thenReturn(new Tax(new Money(0.23), "23%"));

        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxMock);

        Assertions.assertEquals(0, invoice.getItems().size());

    }

    @Test
    public void invoiceGetItemsShouldReturnProperInformation() {

        taxMock = mock(TaxPolicy.class);
        when(taxMock.calculateTax(ProductType.STANDARD, new Money(3))).thenReturn(new Tax(new Money(0.23), "23%"));
        when(taxMock.calculateTax(ProductType.FOOD, new Money(5))).thenReturn(new Tax(new Money(0.46), "46%"));

        productData = mock(ProductData.class);
        when(productData.getType()).thenReturn(ProductType.STANDARD);
        when(productData.getName()).thenReturn("product1");

        ProductData productData2 = mock(ProductData.class);
        when(productData2.getType()).thenReturn(ProductType.FOOD);
        when(productData2.getName()).thenReturn("product2");

        RequestItem requestItem = new RequestItem(productData, 5, new Money(3));
        RequestItem requestItem2 = new RequestItem(productData2, 6, new Money(5));
        invoiceRequest.add(requestItem);
        invoiceRequest.add(requestItem2);

        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxMock);

        List<String> productList = new ArrayList<>();
        productList.add("product1");
        productList.add("product2");

        List<ProductType> productTypeList = new ArrayList<>();
        productTypeList.add(ProductType.STANDARD);
        productTypeList.add(ProductType.FOOD);

        for (int i = 0; i < productList.size(); i++) {
            Assertions.assertEquals(productList.get(i), invoice.getItems().get(i).getProduct().getName());
            Assertions.assertEquals(productTypeList.get(i), invoice.getItems().get(i).getProduct().getType());
        }

    }

    @Test
    public void invoiceRequestShouldCallRequestItemGetTotalCostTwoTimesTest() {

        Money money1 = new Money(3);

        TaxPolicy taxPolicy1 = mock(TaxPolicy.class);
        when(taxPolicy1.calculateTax(ProductType.STANDARD, money1)).thenReturn(new Tax(new Money(0.23), "23%"));

        ProductData productData1 = mock(ProductData.class);
        when(productData1.getType()).thenReturn(ProductType.STANDARD);

        RequestItem requestItem1 = mock(RequestItem.class);
        when(requestItem1.getTotalCost()).thenReturn(new Money(3));
        when(requestItem1.getProductData()).thenReturn(productData1);
        when(requestItem1.getQuantity()).thenReturn(5);
        invoiceRequest.add(requestItem1);
        invoiceRequest.add(requestItem1);

        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicy1);

        verify(requestItem1, times(2)).getTotalCost();

    }
}