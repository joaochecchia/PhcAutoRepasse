package repasse.phcauto.backend.fipe.internal.infrastructure.adapter.out.http;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.*;
/** Interrompe o download antes que a resposta externa possa consumir memória ilimitada. */
final class RespostaLimitadaFipe implements HttpResponse.BodySubscriber<byte[]> {
    private final HttpResponse.BodySubscriber<byte[]> delegate=HttpResponse.BodySubscribers.ofByteArray();
    private final int limite;
    private long recebido;
    private Flow.Subscription subscription;
    RespostaLimitadaFipe(int limite){this.limite=limite;}
    public CompletionStage<byte[]> getBody(){return delegate.getBody();}
    public void onSubscribe(Flow.Subscription s){subscription=s;delegate.onSubscribe(s);}
    public void onNext(List<ByteBuffer> buffers){
        for(var b:buffers)recebido+=b.remaining();
        if(recebido>limite){subscription.cancel();delegate.onError(new IllegalArgumentException("Resposta FIPE excedeu o limite"));}
        else delegate.onNext(buffers);
    }
    public void onError(Throwable t){delegate.onError(t);}
    public void onComplete(){delegate.onComplete();}
}
