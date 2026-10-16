package repasse.phcauto.backend.fipe.internal.infrastructure.adapter.out.http;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.Flow;
import org.junit.jupiter.api.Test;
class RespostaLimitadaFipeTests {
    @Test void interrompeRespostaAcimaDoLimiteAntesDeAcumular(){
        var subscriber=new RespostaLimitadaFipe(4);var subscription=mock(Flow.Subscription.class);
        subscriber.onSubscribe(subscription);
        subscriber.onNext(List.of(ByteBuffer.wrap(new byte[]{1,2,3})));
        subscriber.onNext(List.of(ByteBuffer.wrap(new byte[]{4,5})));
        verify(subscription).cancel();
        assertThatThrownBy(()->subscriber.getBody().toCompletableFuture().join()).hasCauseInstanceOf(IllegalArgumentException.class);
    }
    @Test void aceitaRespostaNoLimite(){
        var subscriber=new RespostaLimitadaFipe(4);var subscription=mock(Flow.Subscription.class);
        subscriber.onSubscribe(subscription);subscriber.onNext(List.of(ByteBuffer.wrap(new byte[]{1,2,3,4})));subscriber.onComplete();
        assertThat(subscriber.getBody().toCompletableFuture().join()).containsExactly(1,2,3,4);verify(subscription,never()).cancel();
    }
}
