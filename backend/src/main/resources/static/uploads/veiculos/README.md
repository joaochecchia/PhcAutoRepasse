# Fotos locais de veículos

Esta é a raiz local dos arquivos enviados para os anúncios.

Os arquivos são organizados assim:

```text
usuarios/{usuarioId}/veiculos/{veiculoId}/{fotoId}.{extensao}
```

Os uploads são armazenados no sistema de arquivos e ignorados pelo Git. O PostgreSQL guarda somente a chave relativa do arquivo.
