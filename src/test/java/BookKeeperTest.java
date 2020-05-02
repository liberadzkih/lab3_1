import org.junit.Before;
import org.junit.Test;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.ClientData;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;
import pl.com.bottega.ecommerce.sales.domain.invoicing.*;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductData;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductType;
import pl.com.bottega.ecommerce.sharedkernel.Money;
import java.math.BigDecimal;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;

public class BookKeeperTest {

    private BookKeeper bookKeeper;
    private TaxPolicy taxPolicyMock;
    private RequestItem item1;
    private RequestItem item2;
    private InvoiceRequest request;

    @Before
    public void before() {
        setupBookKeeper();
        setupTaxPolicyMock();
        setupRequest();
        setupExampleItems();
    }

    private void setupBookKeeper() {
        InvoiceFactory factory = new InvoiceFactory();
        bookKeeper = new BookKeeper(factory);
    }

    private void setupTaxPolicyMock() {
        taxPolicyMock = mock(TaxPolicy.class);
        Tax resultTax = new Tax(new Money(BigDecimal.ZERO), "");
        when(taxPolicyMock.calculateTax(any(), any())).thenReturn(resultTax);
    }

    private void setupRequest() {
        request = new InvoiceRequest(new ClientData(Id.generate(), "Karol"));
    }

    private void setupExampleItems() {
        ProductData product = ProductData.buildCustomProductData()
                                         .withName("Apples")
                                         .withType(ProductType.FOOD)
                                         .withPrice(new Money(BigDecimal.valueOf(5)))
                                         .build();
        ProductData product2 = ProductData.buildSimpleProductDataForTests();
        item1 = new RequestItem(product, 1, product.getPrice());
        item2 = new RequestItem(product2, 1, product2.getPrice());
    }


    @Test
    public void demand_for_invoice_with_one_item_should_return_correct_invoice() {
        //given
        request.add(item1);
        //when
        Invoice result = bookKeeper.issuance(request, taxPolicyMock);
        //then
        assertThat(result.getItems(), hasSize(1)); //hamcrest
        //assertEquals(1, result.getItems().size());  //JUnit4
    }

    @Test
    public void demand_for_invoice_with_zero_item_should_return_zero() {
        Invoice result = bookKeeper.issuance(request, taxPolicyMock);

        assertThat(result.getItems(), hasSize(0));
    }

    @Test
    public void demand_for_invoice_with_two_item_should_return_two() {
        request.add(item1);
        request.add(item2);

        Invoice result = bookKeeper.issuance(request, taxPolicyMock);

        assertThat(result.getItems(), hasSize(2));
    }

    @Test
    public void demand_for_invoice_with_two_items_should_invoke_taxPolicy_two_times() {
        //given
        request.add(item1);
        request.add(item2);
        //when
        bookKeeper.issuance(request, taxPolicyMock);
        //then
        verify(taxPolicyMock).calculateTax(item1.getProductData().getType(), item1.getProductData().getPrice());
        verify(taxPolicyMock).calculateTax(item2.getProductData().getType(), item2.getProductData().getPrice());
    }

    @Test
    public void demand_for_invoice_with_one_items_should_invoke_taxPolicy_one_time() {
        request.add(item1);

        bookKeeper.issuance(request, taxPolicyMock);

        verify(taxPolicyMock).calculateTax(item1.getProductData().getType(), item1.getProductData().getPrice());
    }

    @Test
    public void demand_for_invoice_with_four_items_should_invoke_taxPolicy_four_times() {
        request.add(item1);
        request.add(item1);
        request.add(item1);

        bookKeeper.issuance(request, taxPolicyMock);

        verify(taxPolicyMock,times(3)).calculateTax(item1.getProductData().getType(), item1.getProductData().getPrice());
    }

}
