package repasse.phcauto.backend.anuncios.internal.core.domain;

import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/** Vocabulário canônico usado para evitar fabricantes e modelos duplicados no catálogo. */
public final class CatalogoReferenciaVeicular {
    private CatalogoReferenciaVeicular() { }

    public record Opcao(String valor, List<String> aliases) { }

    private static final Map<Opcao, List<String>> CARROS = catalogo();
    private static final Map<Opcao, List<String>> MOTOS = catalogoMotos();
    private static final Map<Opcao, List<String>> CAMINHOES = catalogoCaminhoes();
    public static final List<String> CORES = List.of("Amarelo", "Azul", "Bege", "Branco", "Cinza", "Dourado", "Laranja", "Marrom", "Prata", "Preto", "Roxo", "Verde", "Vermelho");
    public static final List<String> CARROCERIAS = List.of("Conversível", "Coupé", "Hatch", "Minivan", "Picape", "Sedã", "SUV", "Utilitário", "Van");
    public static final List<String> CAMBIOS = List.of("Automático", "Automatizado", "CVT", "Manual");
    public static final List<String> COMBUSTIVEIS = List.of("Diesel", "Elétrico", "Etanol", "Flex", "Gasolina", "Gás natural", "Híbrido");
    public static final List<String> MOTORIZACOES = List.of("1.0", "1.0 Turbo", "1.3", "1.3 Turbo", "1.4", "1.4 Turbo", "1.5", "1.5 Turbo", "1.6", "1.8", "2.0", "2.0 Turbo", "2.2", "2.4", "2.5", "3.0", "3.0 Turbo", "3.2", "3.5", "4.0", "Elétrico", "Híbrido");
    public static final List<String> TIPOS_FREIO = List.of("ABS", "Disco nas quatro rodas", "Disco ventilado", "Tambor traseiro");
    public static final List<String> TRACOES = List.of("4x2", "4x4", "AWD", "Dianteira", "Integral", "Traseira");
    public static final List<String> DIRECOES = List.of("Elétrica", "Eletro-hidráulica", "Hidráulica", "Mecânica");
    public static final List<String> CATEGORIAS_MOTO = List.of("Big Trail", "Ciclomotor", "Custom", "Naked", "Off-road", "Scooter", "Sport Touring", "Street", "Superesportiva", "Trail", "Touring", "Triciclo");
    public static final List<String> PARTIDAS_MOTO = List.of("Elétrica", "Elétrica e pedal", "Pedal");
    public static final List<String> REFRIGERACOES_MOTO = List.of("Ar", "Ar e óleo", "Líquida", "Óleo");
    public static final List<String> CAMBIOS_MOTO = List.of("Automático DCT", "CVT", "Manual sequencial", "Semiautomático");
    public static final List<String> TIPOS_FREIO_MOTO = List.of(
            "ABS", "CBS", "Disco dianteiro e tambor traseiro", "Disco nas duas rodas",
            "Disco duplo dianteiro e disco traseiro", "Tambor nas duas rodas");
    public static final List<String> CONFIGURACOES_CAMINHAO = List.of(
            "3/4", "Toco 4x2", "Truck 6x2", "Truck 6x4", "Cavalo mecânico 4x2",
            "Cavalo mecânico 6x2", "Cavalo mecânico 6x4", "Rígido 8x2", "Rígido 8x4");
    public static final List<String> CARROCERIAS_CAMINHAO = List.of(
            "Baú", "Baú frigorífico", "Basculante", "Carga seca", "Chassi", "Graneleiro",
            "Munck", "Prancha", "Sider", "Tanque");
    public static final List<String> CAMBIOS_CAMINHAO = List.of(
            "Automático", "Automatizado", "I-Shift", "Manual", "Opticruise", "Powershift", "TraXon");
    public static final List<String> TRACOES_CAMINHAO = List.of("4x2", "4x4", "6x2", "6x4", "6x6", "8x2", "8x4");
    public static final List<String> DIRECOES_CAMINHAO = List.of("Elétrica", "Eletro-hidráulica", "Hidráulica", "Mecânica");
    public static final List<String> TIPOS_FREIO_CAMINHAO = List.of(
            "ABS com freio a ar", "Disco com ABS/EBS", "Freio a ar", "Freio motor", "Tambor com ABS");
    public static final List<String> IMPLEMENTOS_CAMINHAO = List.of(
            "Baú", "Baú frigorífico", "Basculante", "Betoneira", "Caçamba", "Carga seca",
            "Compactador de lixo", "Guindaste/Munck", "Plataforma", "Prancha", "Sider", "Tanque");

    public static List<Opcao> fabricantes() { return List.copyOf(CARROS.keySet()); }

    public static Map<String, List<String>> modelosPorFabricante() {
        var resultado = new LinkedHashMap<String, List<String>>();
        CARROS.forEach((fabricante, modelos) -> resultado.put(fabricante.valor(), modelos));
        return Map.copyOf(resultado);
    }

    public static List<Opcao> fabricantesMotos() { return List.copyOf(MOTOS.keySet()); }

    public static Map<String, List<String>> modelosMotosPorFabricante() {
        var resultado = new LinkedHashMap<String, List<String>>();
        MOTOS.forEach((fabricante, modelos) -> resultado.put(fabricante.valor(), modelos));
        return Map.copyOf(resultado);
    }

    public static List<Opcao> fabricantesCaminhoes() { return List.copyOf(CAMINHOES.keySet()); }

    public static Map<String, List<String>> modelosCaminhoesPorFabricante() {
        var resultado = new LinkedHashMap<String, List<String>>();
        CAMINHOES.forEach((fabricante, modelos) -> resultado.put(fabricante.valor(), modelos));
        return Map.copyOf(resultado);
    }

    public static Optional<String> normalizarFabricante(String informado) {
        String chave = chave(informado);
        return CARROS.keySet().stream()
                .filter(opcao -> chave(opcao.valor()).equals(chave)
                        || opcao.aliases().stream().map(CatalogoReferenciaVeicular::chave).anyMatch(chave::equals))
                .map(Opcao::valor).findFirst();
    }

    public static Optional<String> normalizarModelo(String fabricante, String informado) {
        return normalizarFabricante(fabricante).flatMap(canonico -> CARROS.entrySet().stream()
                .filter(entry -> entry.getKey().valor().equals(canonico))
                .flatMap(entry -> entry.getValue().stream())
                .filter(modelo -> chave(modelo).equals(chave(informado)))
                .findFirst());
    }

    public static Optional<String> normalizarFabricanteMoto(String informado) {
        String chave = chave(informado);
        return MOTOS.keySet().stream()
                .filter(opcao -> chave(opcao.valor()).equals(chave)
                        || opcao.aliases().stream().map(CatalogoReferenciaVeicular::chave).anyMatch(chave::equals))
                .map(Opcao::valor).findFirst();
    }

    public static Optional<String> normalizarModeloMoto(String fabricante, String informado) {
        return normalizarFabricanteMoto(fabricante).flatMap(canonico -> MOTOS.entrySet().stream()
                .filter(entry -> entry.getKey().valor().equals(canonico))
                .flatMap(entry -> entry.getValue().stream())
                .filter(modelo -> chave(modelo).equals(chave(informado)))
                .findFirst());
    }

    public static Optional<String> normalizarFabricanteCaminhao(String informado) {
        String chave = chave(informado);
        return CAMINHOES.keySet().stream()
                .filter(opcao -> chave(opcao.valor()).equals(chave)
                        || opcao.aliases().stream().map(CatalogoReferenciaVeicular::chave).anyMatch(chave::equals))
                .map(Opcao::valor).findFirst();
    }

    public static Optional<String> normalizarModeloCaminhao(String fabricante, String informado) {
        return normalizarFabricanteCaminhao(fabricante).flatMap(canonico -> CAMINHOES.entrySet().stream()
                .filter(entry -> entry.getKey().valor().equals(canonico))
                .flatMap(entry -> entry.getValue().stream())
                .filter(modelo -> chave(modelo).equals(chave(informado)))
                .findFirst());
    }

    public static Optional<String> normalizarOpcao(List<String> opcoes, String informado) {
        if (informado == null || informado.isBlank()) return Optional.empty();
        return opcoes.stream().filter(opcao -> chave(opcao).equals(chave(informado))).findFirst();
    }

    private static String chave(String valor) {
        if (valor == null) return "";
        return Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").replaceAll("[^a-zA-Z0-9]", "")
                .toLowerCase(Locale.ROOT);
    }

    private static Map<Opcao, List<String>> catalogo() {
        var itens = new LinkedHashMap<Opcao, List<String>>();
        itens.put(new Opcao("Volkswagen", List.of("VW", "Volks")), List.of("Amarok", "Apollo", "Bora", "Brasília", "CrossFox", "Eos", "Fox", "Fusca", "Gol", "Golf", "ID.4", "Jetta", "Kombi", "Logus", "Nivus", "Parati", "Passat", "Pointer", "Polo", "Santana", "Saveiro", "SpaceFox", "T-Cross", "Taos", "Tiguan", "Touareg", "Up!", "Variant", "Virtus", "Voyage"));
        itens.put(new Opcao("Chevrolet", List.of("Chevy", "GM")), List.of("Agile", "Astra", "Blazer", "Bolt", "Bonanza", "Brasinca", "C-10", "C-20", "Calibra", "Camaro", "Captiva", "Caravan", "Celta", "Chevette", "Classic", "Cobalt", "Colorado", "Corsa", "Cruze", "D-20", "Equinox", "Ipanema", "Kadett", "Malibu", "Marajó", "Meriva", "Montana", "Monza", "Omega", "Onix", "Opala", "Prisma", "S10", "Silverado", "Sonic", "Spin", "Suprema", "Tracker", "Trailblazer", "Vectra", "Veraneio", "Zafira"));
        itens.put(new Opcao("Fiat", List.of()), List.of("147", "500", "Argo", "Brava", "Bravo", "Cronos", "Doblo", "Ducato", "Elba", "Fastback", "Fiorino", "Freemont", "Grand Siena", "Idea", "Linea", "Marea", "Mobi", "Oggi", "Palio", "Panorama", "Premio", "Pulse", "Punto", "Siena", "Stilo", "Strada", "Tempra", "Tipo", "Toro", "Uno"));
        itens.put(new Opcao("Toyota", List.of()), List.of("Avalon", "Bandeirante", "Camry", "Celica", "Corolla", "Corolla Cross", "Corona", "Etios", "Fielder", "Hilux", "Land Cruiser", "Prius", "RAV4", "Supra", "SW4", "Yaris"));
        itens.put(new Opcao("Honda", List.of()), List.of("Accord", "City", "Civic", "CR-V", "CR-Z", "Fit", "HR-V", "Odyssey", "Prelude", "WR-V", "ZR-V"));
        itens.put(new Opcao("Hyundai", List.of()), List.of("Azera", "Creta", "Elantra", "HB20", "HB20S", "i30", "ix35", "Santa Fe", "Tucson", "Veracruz"));
        itens.put(new Opcao("Jeep", List.of()), List.of("Cherokee", "Commander", "Compass", "Gladiator", "Grand Cherokee", "Renegade", "Wrangler"));
        itens.put(new Opcao("Renault", List.of()), List.of("Captur", "Clio", "Duster", "Fluence", "Kangoo", "Kardian", "Kwid", "Logan", "Master", "Megane", "Oroch", "Sandero", "Scenic", "Symbol"));
        itens.put(new Opcao("Ford", List.of()), List.of("Bronco", "Courier", "EcoSport", "Edge", "Escort", "Expedition", "F-150", "Fiesta", "Focus", "Fusion", "Ka", "Maverick", "Mustang", "Ranger", "Territory"));
        itens.put(new Opcao("Nissan", List.of()), List.of("Frontier", "Kicks", "Leaf", "Livina", "March", "Pathfinder", "Sentra", "Tiida", "Versa", "X-Trail"));
        itens.put(new Opcao("Citroën", List.of("Citroen")), List.of("Aircross", "Berlingo", "C3", "C3 Aircross", "C4", "C4 Cactus", "C5", "Jumpy", "Xsara Picasso"));
        itens.put(new Opcao("Peugeot", List.of()), List.of("2008", "206", "207", "208", "3008", "307", "308", "408", "5008", "Partner"));
        itens.put(new Opcao("Mitsubishi", List.of("Mitsu")), List.of("ASX", "Eclipse Cross", "L200", "Lancer", "Outlander", "Pajero", "Pajero Sport"));
        itens.put(new Opcao("Kia", List.of()), List.of("Bongo", "Carnival", "Cerato", "Mohave", "Niro", "Picanto", "Sorento", "Soul", "Sportage", "Stonic"));
        itens.put(new Opcao("BMW", List.of()), List.of("Série 1", "Série 2", "Série 3", "Série 4", "Série 5", "Série 7", "M3", "M5", "X1", "X2", "X3", "X4", "X5", "X6", "X7"));
        itens.put(new Opcao("Mercedes-Benz", List.of("Mercedes", "MB")), List.of("Classe A", "Classe B", "Classe C", "Classe E", "Classe G", "CLA", "CLE", "GLA", "GLB", "GLC", "GLE", "GLS", "Sprinter"));
        itens.put(new Opcao("Audi", List.of()), List.of("A1", "A3", "A4", "A5", "A6", "A7", "A8", "Q3", "Q5", "Q7", "Q8", "R8", "RS3", "RS5", "RS6", "RS7", "TT"));
        itens.put(new Opcao("Volvo", List.of()), List.of("C40", "EX30", "S60", "S90", "V40", "XC40", "XC60", "XC90"));
        itens.put(new Opcao("Land Rover", List.of("LR")), List.of("Defender", "Discovery", "Discovery Sport", "Freelander", "Range Rover", "Range Rover Evoque", "Range Rover Sport", "Range Rover Velar"));
        itens.put(new Opcao("Porsche", List.of()), List.of("718", "911", "Boxster", "Cayenne", "Cayman", "Macan", "Panamera", "Taycan"));
        itens.put(new Opcao("BYD", List.of()), List.of("Dolphin", "Dolphin Mini", "Han", "King", "Seal", "Seal U", "Shark", "Song Plus", "Song Pro", "Tan", "Yuan Plus", "Yuan Pro"));
        itens.put(new Opcao("GWM", List.of("Great Wall", "Great Wall Motors", "Haval")), List.of("Haval H6", "Haval H9", "Ora 03", "Poer P30", "Tank 300", "Tank 500"));
        itens.put(new Opcao("Chery", List.of("Caoa Chery", "CAOA")), List.of("Arrizo 5", "Arrizo 6", "Celer", "Face", "QQ", "Tiggo 2", "Tiggo 3X", "Tiggo 5X", "Tiggo 7", "Tiggo 8"));
        itens.put(new Opcao("RAM", List.of("Dodge Ram")), List.of("1500", "2500", "3500", "Classic", "Rampage"));
        itens.put(new Opcao("Suzuki", List.of()), List.of("Grand Vitara", "Jimny", "S-Cross", "Swift", "Vitara"));
        itens.put(new Opcao("Subaru", List.of()), List.of("Forester", "Impreza", "Legacy", "Outback", "WRX", "XV"));
        itens.put(new Opcao("Lexus", List.of()), List.of("ES", "NX", "RX", "UX"));
        itens.put(new Opcao("JAC", List.of("JAC Motors")), List.of("E-JS1", "E-JS4", "J3", "J5", "J6", "T40", "T50", "T60", "T80"));
        itens.put(new Opcao("Mini", List.of("MINI Cooper")), List.of("Clubman", "Cooper", "Countryman", "Paceman"));
        itens.put(new Opcao("GAC", List.of("GAC Motor", "GAC Motors")), List.of("Aion ES", "Aion V", "Aion Y", "Emkoo", "Empow", "GS3", "GS4", "GS8", "Hyptec HT"));
        itens.put(new Opcao("Geely", List.of()), List.of("Coolray", "EX2", "EX5", "Monjaro", "Okavango"));
        itens.put(new Opcao("Zeekr", List.of()), List.of("001", "007", "7X", "Mix", "X"));
        itens.put(new Opcao("Omoda", List.of()), List.of("Omoda 5", "Omoda 5 SHS-H", "Omoda 7", "Omoda 7 SHS-P", "Omoda E5"));
        itens.put(new Opcao("Jaecoo", List.of()), List.of("Jaecoo 5", "Jaecoo 7", "Jaecoo 7 SHS-P", "Jaecoo 8"));
        itens.put(new Opcao("Leapmotor", List.of()), List.of("B10", "C10", "C16", "T03"));
        itens.put(new Opcao("Neta", List.of("Neta Auto")), List.of("Aya", "GT", "L", "S", "X"));
        itens.put(new Opcao("Seres", List.of()), List.of("3", "5", "7"));
        itens.put(new Opcao("Abarth", List.of()), List.of("500e", "Fastback", "Pulse"));
        itens.put(new Opcao("Alfa Romeo", List.of("Alfa")), List.of("145", "156", "164", "Giulia", "Giulietta", "Stelvio"));
        itens.put(new Opcao("Dodge", List.of()), List.of("Challenger", "Charger", "Dakota", "Durango", "Journey"));
        itens.put(new Opcao("Ferrari", List.of()), List.of("296", "458 Italia", "488", "California", "F8 Tributo", "Portofino", "Purosangue", "Roma", "SF90"));
        itens.put(new Opcao("Maserati", List.of()), List.of("Ghibli", "GranCabrio", "GranTurismo", "Grecale", "Levante", "MC20", "Quattroporte"));
        itens.put(new Opcao("Jaguar", List.of()), List.of("E-Pace", "F-Pace", "F-Type", "I-Pace", "S-Type", "XE", "XF", "XJ"));
        itens.put(new Opcao("Bentley", List.of()), List.of("Bentayga", "Continental GT", "Flying Spur", "Mulsanne"));
        itens.put(new Opcao("Lamborghini", List.of()), List.of("Aventador", "Gallardo", "Huracán", "Revuelto", "Urus"));
        itens.put(new Opcao("McLaren", List.of()), List.of("570S", "600LT", "720S", "750S", "Artura", "GT"));
        itens.put(new Opcao("Rolls-Royce", List.of("Rolls Royce")), List.of("Cullinan", "Dawn", "Ghost", "Phantom", "Spectre", "Wraith"));
        itens.put(new Opcao("Troller", List.of()), List.of("Pantanal", "RF Sport", "T4"));
        itens.put(new Opcao("SsangYong", List.of("KGM")), List.of("Actyon", "Korando", "Kyron", "Musso", "Rexton", "Tivoli"));
        itens.put(new Opcao("Lifan", List.of()), List.of("320", "520", "530", "620", "Foison", "X60", "X80"));
        itens.put(new Opcao("Agrale", List.of()), List.of("Marruá"));
        return Map.copyOf(itens);
    }

    private static Map<Opcao, List<String>> catalogoMotos() {
        var itens = new LinkedHashMap<Opcao, List<String>>();
        itens.put(new Opcao("Honda", List.of("Honda Motos")), List.of("ADV", "Africa Twin", "Biz 110i", "Biz 125", "CB 300F Twister", "CB 500F", "CB 500X", "CB 650R", "CB 750 Hornet", "CB 1000 Hornet", "CBR 500R", "CBR 650R", "CBR 1000RR-R", "CG 125", "CG 150", "CG 160", "Elite 125", "Gold Wing", "NC 750X", "NX 500", "NXR 160 Bros", "PCX", "Pop 100", "Pop 110i", "Sahara 300", "Tornado 300", "Transalp 750", "X-ADV", "XRE 190", "XRE 300"));
        itens.put(new Opcao("Yamaha", List.of("Yamaha Motor")), List.of("Aerox", "Crosser 150", "Factor 125", "Factor 150", "Fazer 150", "Fazer 250", "Fazer FZ15", "Fazer FZ25", "Fluo", "Lander 250", "MT-03", "MT-07", "MT-09", "Neo", "NMax", "R15", "R3", "R7", "Ténéré 250", "Ténéré 700", "Tracer 7", "TT-R 230", "WR250F", "XJ6", "XMax", "XT 660", "YZ125", "YZ250F", "YZ450F", "YZF-R1", "YZF-R6"));
        itens.put(new Opcao("Suzuki", List.of("Suzuki Motos")), List.of("Bandit 650", "Boulevard M800", "Burgman", "DR 160", "DR 650", "GS 500", "GSR 750", "GSX-8S", "GSX-R 750", "GSX-R 1000", "GSX-S 750", "GSX-S 1000", "Hayabusa", "Intruder 125", "Katana", "V-Strom 650", "V-Strom 800", "V-Strom 1050"));
        itens.put(new Opcao("Kawasaki", List.of()), List.of("Eliminator 500", "KLR 650", "Ninja 300", "Ninja 400", "Ninja 500", "Ninja 650", "Ninja ZX-4R", "Ninja ZX-6R", "Ninja ZX-10R", "Versys 300", "Versys 650", "Versys 1000", "Vulcan S", "Z300", "Z400", "Z500", "Z650", "Z900", "Z1000"));
        itens.put(new Opcao("BMW Motorrad", List.of("BMW", "BMW Motos")), List.of("C 400 X", "F 750 GS", "F 800 GS", "F 850 GS", "F 900 GS", "F 900 R", "G 310 GS", "G 310 R", "K 1600 GTL", "M 1000 R", "M 1000 RR", "R 1200 GS", "R 1250 GS", "R 1300 GS", "R 18", "S 1000 R", "S 1000 RR", "S 1000 XR"));
        itens.put(new Opcao("Ducati", List.of()), List.of("Diavel", "DesertX", "Hypermotard", "Monster", "Multistrada V2", "Multistrada V4", "Panigale V2", "Panigale V4", "Scrambler", "Streetfighter V2", "Streetfighter V4", "SuperSport"));
        itens.put(new Opcao("Triumph", List.of()), List.of("Bonneville Bobber", "Bonneville T100", "Bonneville T120", "Daytona 660", "Rocket 3", "Scrambler 400 X", "Scrambler 900", "Speed 400", "Speed Triple", "Street Triple", "Tiger 660", "Tiger 900", "Tiger 1200", "Trident 660"));
        itens.put(new Opcao("Harley-Davidson", List.of("Harley", "HD")), List.of("Breakout", "CVO", "Fat Bob", "Fat Boy", "Heritage Classic", "Iron 883", "Low Rider", "Nightster", "Pan America", "Road Glide", "Sport Glide", "Sportster S", "Street Bob", "Street Glide", "Ultra Limited"));
        itens.put(new Opcao("Royal Enfield", List.of("RE")), List.of("Bear 650", "Bullet 350", "Classic 350", "Classic 650", "Continental GT 650", "Goan Classic 350", "Guerrilla 450", "Himalayan 411", "Himalayan 450", "Hunter 350", "Interceptor 650", "Meteor 350", "Scram 411", "Shotgun 650", "Super Meteor 650"));
        itens.put(new Opcao("KTM", List.of()), List.of("125 Duke", "200 Duke", "250 Duke", "390 Adventure", "390 Duke", "690 Enduro R", "790 Adventure", "890 Adventure", "1290 Super Adventure", "RC 390"));
        itens.put(new Opcao("Bajaj", List.of()), List.of("Dominar 160", "Dominar 200", "Dominar 250", "Dominar 400", "Pulsar N150", "Pulsar N160", "Pulsar NS200"));
        itens.put(new Opcao("Shineray", List.of()), List.of("Free 150", "Iron 250", "Jet 50", "Jet 125", "JEF 150", "Phoenix 50", "SHI 175", "Storm 200", "Worker 125", "Worker 150", "XY 50"));
        itens.put(new Opcao("Haojue", List.of()), List.of("Chopper Road 150", "DK 150", "DR 160", "Lindy 125", "Master Ride 150", "Nex 115", "NK 150", "VR 150"));
        itens.put(new Opcao("Dafra", List.of()), List.of("Apache RTR 200", "Citycom 300", "Cruisym 150", "Cruisym 300", "Horizon 150", "Maxsym 400", "NH 190", "NH 300", "Next 250", "Symphony 125"));
        itens.put(new Opcao("Indian", List.of("Indian Motorcycle")), List.of("Challenger", "Chief", "Chieftain", "FTR", "Pursuit", "Roadmaster", "Scout", "Scout Bobber", "Springfield"));
        itens.put(new Opcao("Vespa", List.of()), List.of("GTS", "GTS Super", "Primavera", "Sprint", "VXL"));
        itens.put(new Opcao("Piaggio", List.of()), List.of("Beverly", "Liberty", "Medley", "MP3"));
        itens.put(new Opcao("Aprilia", List.of()), List.of("RS 660", "RSV4", "Tuareg 660", "Tuono 660", "Tuono V4"));
        itens.put(new Opcao("Moto Guzzi", List.of("Guzzi")), List.of("Stelvio", "V7", "V85 TT", "V100 Mandello"));
        itens.put(new Opcao("Husqvarna", List.of()), List.of("701 Enduro", "701 Supermoto", "Norden 901", "Svartpilen 401", "Vitpilen 401"));
        itens.put(new Opcao("MV Agusta", List.of()), List.of("Brutale", "Dragster", "F3", "Rush", "Turismo Veloce"));
        itens.put(new Opcao("Can-Am", List.of("Can Am")), List.of("Ryker", "Spyder F3", "Spyder RT"));
        itens.put(new Opcao("Zontes", List.of()), List.of("R 350", "S 350", "T 350", "V 350"));
        return Map.copyOf(itens);
    }

    private static Map<Opcao, List<String>> catalogoCaminhoes() {
        var itens = new LinkedHashMap<Opcao, List<String>>();
        itens.put(new Opcao("Agrale", List.of()), List.of("A8700", "A10000", "A14000", "A18000", "A20000"));
        itens.put(new Opcao("DAF", List.of()), List.of("CF", "CF 85", "XF", "XF 105", "XF 480", "XF 530"));
        itens.put(new Opcao("Foton", List.of()), List.of("Aumark S", "Aumark 315", "Aumark 916", "Aumark 1217", "Auman"));
        itens.put(new Opcao("Ford", List.of("Ford Caminhões")), List.of("Cargo 816", "Cargo 1119", "Cargo 1319", "Cargo 1519", "Cargo 1719", "Cargo 2429", "Cargo 2629", "Cargo 3133", "F-4000"));
        itens.put(new Opcao("Hyundai", List.of("Hyundai Caminhões")), List.of("HD78", "HD80", "HD120", "Mighty EX8"));
        itens.put(new Opcao("International", List.of()), List.of("9800i", "DuraStar", "MV", "ProStar"));
        itens.put(new Opcao("Iveco", List.of()), List.of("Daily 35-160", "Daily 55-180", "Daily 70-180", "EuroCargo", "Hi-Road", "Hi-Way", "S-Way", "Tector 9-190", "Tector 11-190", "Tector 17-280", "Tector 24-300"));
        itens.put(new Opcao("JAC", List.of("JAC Motors")), List.of("iEV1200T", "N350", "N55", "N80", "N90", "N120"));
        itens.put(new Opcao("MAN", List.of()), List.of("TGX 28.440", "TGX 29.480", "TGX 33.440", "TGS", "TGM"));
        itens.put(new Opcao("Mercedes-Benz", List.of("Mercedes", "MB")), List.of("Accelo 815", "Accelo 1016", "Actros 2045", "Actros 2548", "Actros 2651", "Arocs", "Atego 1419", "Atego 1719", "Atego 2429", "Axor 1933", "Axor 2544", "Axor 2644", "L 1113", "L 1620", "L 1622", "LS 1935", "Sprinter"));
        itens.put(new Opcao("Scania", List.of()), List.of("G 410", "G 440", "G 500", "P 250", "P 310", "P 360", "R 440", "R 450", "R 500", "R 540", "R 560", "R 620", "S 500", "S 540", "T 113", "T 124"));
        itens.put(new Opcao("Shacman", List.of()), List.of("X3000", "X5000", "X6000"));
        itens.put(new Opcao("Sinotruk", List.of("Sitrak")), List.of("Howo", "Sitrak C7H", "Sitrak G7X"));
        itens.put(new Opcao("Volkswagen Caminhões e Ônibus", List.of("Volkswagen", "VW", "VWCO")), List.of("Delivery 6.160", "Delivery 9.170", "Delivery 11.180", "Constellation 17.280", "Constellation 24.280", "Constellation 25.460", "Constellation 26.280", "Constellation 31.320", "Constellation 33.460", "Meteor 28.480", "Meteor 29.530", "Worker 13.180", "Worker 17.220", "Worker 24.250"));
        itens.put(new Opcao("Volvo", List.of("Volvo Caminhões")), List.of("FH 420", "FH 440", "FH 460", "FH 500", "FH 520", "FH 540", "FM 370", "FM 410", "FM 460", "FMX 460", "VM 270", "VM 290", "VM 330"));
        return Map.copyOf(itens);
    }
}
