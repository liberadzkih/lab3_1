package pl.com.bottega.ecommerce.sales.domain.invoicing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.ClientData;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.Product;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductData;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductType;
import pl.com.bottega.ecommerce.sharedkernel.Money;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) //daje mozliwosc korzystania z mockito -> umozliwia tworzenie dublerow
class BookKeeperTest {

    @Mock  //dubler dla zaleznosci
    TaxPolicy taxPolicy;

    Id id;
    ClientData clientData;
    InvoiceRequest invoiceRequest;
    BookKeeper bookKeeper;
    Product product;
    ProductData productData;
    RequestItem requestItem;
    Invoice invoice;

    @BeforeEach // dla kazdego testu na nowo tworzymy obiekty
    void setup(){
        id = Id.generate();
        clientData = new ClientData(id,"client");
        invoiceRequest = new InvoiceRequest(clientData);
        bookKeeper = new BookKeeper(new InvoiceFactory());
        product = new Product(Id.generate(),new Money(BigDecimal.TEN),"Pasta",ProductType.FOOD);
        productData = product.generateSnapshot();
        when(taxPolicy.calculateTax(any(ProductType.class),any(Money.class))).thenReturn(new Tax(new Money(BigDecimal.TEN),"tax"));
        requestItem = new RequestItem(productData,4,new Money(BigDecimal.TEN));
    }

    //żądanie wydania faktury z jedną pozycją powinno zwrócić fakturę z jedną pozycją
    @Test
    public void invoiceRequestWithOnePositionReturnInvoiceWithOnePosition(){
        invoiceRequest.add(requestItem);
        invoice = bookKeeper.issuance(invoiceRequest,taxPolicy);
        assertThat(invoice.getItems(),hasSize(1));
    }

    //żądanie wydania faktury z dwiema pozycjami powinno wywołać metodę calculateTax dwa razy
    @Test
    public void invoiceRequestWithTwoPositionShouldInvokeCalculateTaxMethodTwoTimes(){
        invoiceRequest.add(requestItem);
        bookKeeper.issuance(invoiceRequest,taxPolicy);
        verify(taxPolicy).calculateTax(requestItem.getProductData().getType(),requestItem.getProductData().getPrice());
        verify(taxPolicy).calculateTax(requestItem.getProductData().getType(),requestItem.getProductData().getPrice());
    }

    //2 kolejne testy stanu
    //żądanie wydania faktury z 20 pozycjami powinno zwrócić fakturę z 20 pozycjami
    @Test
    public void invoiceRequestWithTwentyPositionsReturnInvoiceWithTwentyPositions(){
        for(int i=0;i<20;i++)
            invoiceRequest.add(requestItem);

        invoice = bookKeeper.issuance(invoiceRequest,taxPolicy);
        assertThat(invoice.getItems(),hasSize(20));
    }

    //żądanie wydania faktury z setka pozycji powinno zwrócić fakturę z setka pozycji
    @Test
    public void invoiceRequestWithOneHundredPositionsReturnInvoiceWithOneHundredPositions(){
        for(int i=0;i<100;i++)
            invoiceRequest.add(requestItem);

        invoice = bookKeeper.issuance(invoiceRequest,taxPolicy);
        assertThat(invoice.getItems(),hasSize(100));
    }

    //2 kolejne testy zachowania

    @Test
    public void invoiceRequestWithTwentyPositionShouldInvokeCalculateTaxMethodTwentyTimes(){
        for(int i=0;i<20;i++)
            invoiceRequest.add(requestItem);

        invoice = bookKeeper.issuance(invoiceRequest,taxPolicy);
        verify(taxPolicy,times(20)).calculateTax(requestItem.getProductData().getType(),requestItem.getProductData().getPrice());
    }

    @Test
    public void invoiceRequestWithOneHundredPositionShouldInvokeCalculateTaxMethodOneHundredTimes(){
        for(int i=0;i<100;i++)
            invoiceRequest.add(requestItem);

        invoice = bookKeeper.issuance(invoiceRequest,taxPolicy);
        verify(taxPolicy,times(100)).calculateTax(requestItem.getProductData().getType(),requestItem.getProductData().getPrice());
    }
}
