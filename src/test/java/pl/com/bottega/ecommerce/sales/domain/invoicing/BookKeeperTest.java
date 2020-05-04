package pl.com.bottega.ecommerce.sales.domain.invoicing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.ClientData;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.Product;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductBuilder;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductType;
import pl.com.bottega.ecommerce.sharedkernel.Money;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BookKeeperTest {

    private TaxPolicy taxMock;
    private Money money;
    private BookKeeper bookKeeper;

    private RequestItem createStandardRequestItem() {
        Product product = new ProductBuilder().withProductType(ProductType.STANDARD)
                                              .withId(Id.generate())
                                              .withName("Item")
                                              .withPrice(money)
                                              .build();
        return new RequestItemBuilder().withProductData(product.generateSnapshot()).withQuantity(1).withTotalCost(money).build();
    }

    @BeforeEach void setUp() {

        money = Money.ZERO;

        taxMock = mock(TaxPolicy.class);
        when(taxMock.calculateTax(any(), any())).thenReturn(new Tax(money, "Tax"));

        bookKeeper = new BookKeeper(new InvoiceFactory());
    }

    //State tests
    @Test void requestingInvoiceWithOneItem_shouldReturnOneItemInvoice() {

        ClientData clientData = new ClientData(Id.generate(), "Client");
        InvoiceRequest request = new InvoiceRequest(clientData);
        RequestItem requestItem = createStandardRequestItem();
        request.add(requestItem);
        Invoice invoice = bookKeeper.issuance(request, taxMock);

        assertEquals(1, invoice.getItems().size());
    }

    @Test void requestingInvoiceWithZeroItems_shouldReturnInvoiceWithNoItems() {

        ClientData clientData = new ClientData(Id.generate(), "Client");
        InvoiceRequest request = new InvoiceRequest(clientData);
        Invoice invoice = bookKeeper.issuance(request, taxMock);

        assertEquals(0, invoice.getItems().size());
    }

    @Test void passingNullAsMethodParameters_shouldThrowNullPointerException() {
        ClientData clientData = new ClientData(Id.generate(), "Client");
        InvoiceRequest request = new InvoiceRequest(clientData);
        RequestItem requestItem = createStandardRequestItem();
        request.add(requestItem);

        assertThrows(NullPointerException.class, () -> bookKeeper.issuance(null, taxMock));
        assertThrows(NullPointerException.class, () -> bookKeeper.issuance(request, null));
    }

    //Behaviour tests
    @Test void requestingInvoiceWithTwoItems_expectedCalculateTaxCallTwice() {

        ClientData clientData = new ClientData(Id.generate(), "Client");
        InvoiceRequest request = new InvoiceRequest(clientData);

        Product firstProduct = new ProductBuilder().withProductType(ProductType.DRUG).withName("Drug").build();
        Product secondProduct = new ProductBuilder().withProductType(ProductType.FOOD).withName("Food").build();
        RequestItem firstItem = new RequestItemBuilder().withProductData(firstProduct.generateSnapshot()).build();
        RequestItem secondItem = new RequestItemBuilder().withProductData(secondProduct.generateSnapshot()).build();
        request.add(firstItem);
        request.add(secondItem);

        bookKeeper.issuance(request, taxMock);

        verify(taxMock).calculateTax(firstItem.getProductData().getType(), money);
        verify(taxMock).calculateTax(secondItem.getProductData().getType(), money);

        verify(taxMock, times(2)).calculateTax(any(), any());
    }

    @Test void requestingInvoiceWithZeroItems_expectedZeroCalculateTaxCalls() {
        ClientData clientData = new ClientData(Id.generate(), "Client");
        InvoiceRequest request = new InvoiceRequest(clientData);

        bookKeeper.issuance(request, taxMock);

        verify(taxMock, never()).calculateTax(any(), any());
    }

    @Test void requestingInvoiceWithOneItem_expectedCalculateTaxCallOnce() {
        ClientData clientData = new ClientData(Id.generate(), "Client");
        InvoiceRequest request = new InvoiceRequest(clientData);

        RequestItem item = createStandardRequestItem();

        request.add(item);

        bookKeeper.issuance(request, taxMock);

        verify(taxMock).calculateTax(item.getProductData().getType(), money);
    }
}
