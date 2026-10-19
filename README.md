# Madoe - Leitor de HQ e Mangá

> *A janela para as suas histórias.*

Android · Kotlin · Jetpack Compose · Material 3 · 100% offline

## Sobre

**Madoe** é um leitor de quadrinhos para Android que lê HQs, gibis, mangás e outros quadrinhos
guardados no próprio aparelho, sem conta, sem internet e sem anúncios. O nome combina 窓 (*mado*,
"janela") e 絵 (*e*, "imagem"): a janela pela qual você vê as histórias.

O projeto começou como um leitor pessoal, para ler HQs e mangás dentro do ônibus e do carro, e foi
crescendo até virar um aplicativo completo, com biblioteca, coleções, leitor com vários modos e
gerenciamento de arquivos. Ele é publicado aqui como projeto de portfólio.

## Funcionalidades

**Biblioteca**

- Importação de arquivos pelo seletor de documentos do Android e adição de pastas, que são
  escaneadas ao abrir o app (a varredura pode ser desligada nas configurações) ou sob demanda.
- Formatos: CBZ, CBR (RAR), CB7 (7z), CBT (TAR), ZIP, PDF e imagens soltas (JPG, PNG e WebP). O tipo
  do arquivo é detectado pelo **conteúdo**, não pela extensão: um `.cbr` que na verdade é um ZIP abre
  normalmente.
- Capas geradas a partir da primeira página; informações de formato, tamanho, páginas e progresso.
- Busca por título e ordenação por nome, adição recente ou última leitura.
- Favoritos, coleções (criar, renomear, excluir e organizar títulos), renomear títulos e
  **remover da biblioteca sem apagar o arquivo original**.
- Telas de Recentes ("Continue lendo" e "Adicionados recentemente"), Arquivos, Favoritos, Coleções e
  Detalhes do título.

**Leitor**

- Modos página a página e rolagem contínua vertical.
- Direção de leitura da esquerda para a direita (HQs) ou da direita para a esquerda (mangás).
- Ajuste da página à largura ou à altura da tela; zoom por pinça (até 5x) no modo página a página.
- Navegação por gestos, por toque nas laterais da tela e por botões; controle deslizante de páginas,
  "ir para página" e marcadores de página.
- Modo noturno, orientação do leitor (automática, vertical ou horizontal), tela cheia e tela sempre
  acesa durante a leitura.
- O progresso é salvo a cada página, e a leitura é retomada exatamente de onde parou.
- Páginas ilegíveis ou corrompidas mostram um aviso com opção de tentar de novo, sem derrubar o app.

**Configurações**

- Tema claro, escuro ou do sistema; preferências de leitura salvas entre sessões.
- Gerenciamento das pastas monitoradas e do armazenamento (capas geradas e cache temporário).
- Introdução na primeira abertura.

## Capturas de tela

Em breve.

<!--
Quando houver imagens, crie a pasta docs/screenshots/, coloque as capturas nela e troque o
"Em breve." acima por uma tabela com as imagens, usando a sintaxe de imagem do Markdown.
Use só quadrinhos de domínio público ou com licença que permita a exibição, e dê o crédito.
-->

## Tecnologias

| Área | Tecnologia |
|---|---|
| Linguagem | Kotlin 2.0.21 (JVM 17) |
| Interface | Jetpack Compose (BOM 2024.12.01), Material 3, Navigation Compose 2.8.5 |
| Arquitetura | MVVM com repositórios e injeção de dependência com Hilt 2.52 |
| Dados locais | Room 2.6.1 (biblioteca, coleções, marcadores) e DataStore 1.1.1 (preferências) |
| Arquivos | Storage Access Framework; Apache Commons Compress 1.27.1 (ZIP, TAR, 7z), XZ for Java 1.10, junrar 8.1.1 (RAR) e `PdfRenderer` do Android (PDF) |
| Imagens | Coil 2.7.0 (capas) e `BitmapFactory` com amostragem (páginas) |
| Build | Android Gradle Plugin 8.7.3, Gradle 8.9 (wrapper), KSP |
| Testes | JUnit 4, AndroidX Test, kotlinx-coroutines-test |

SDK: `minSdk` 26 (Android 8.0), `compileSdk` e `targetSdk` 35. As versões estão em
[`gradle/libs.versions.toml`](gradle/libs.versions.toml).

## Arquitetura

```
app/src/main/java/com/mangareader/app/
├── MadoeApp.kt, MainActivity.kt, MainViewModel.kt
├── data/
│   ├── db/        Room: entidades, DAOs e banco
│   ├── library/   importação, varredura de pastas e tarefas da biblioteca
│   ├── model/     modelos e enums (formatos, configurações, progresso)
│   ├── prefs/     preferências (DataStore)
│   ├── repo/      repositórios
│   ├── source/    leitura dos formatos: uma implementação de PageSource por tipo
│   └── storage/   cache e armazenamento
├── di/            módulos do Hilt
└── ui/
    ├── components/  cartões, barras, diálogos e ações compartilhadas
    ├── navigation/  rotas e barra inferior
    ├── onboarding/  introdução
    ├── reader/      leitor, carregador de páginas com cache e diálogos
    ├── screens/     Recentes, Arquivos, Favoritos, Coleções e Detalhes
    ├── settings/    configurações
    └── theme/       cores, tipografia e formas
```

Decisões de projeto:

- **Offline e sem permissões amplas.** Os arquivos são acessados só pelo seletor de documentos do
  Android (SAF), e o app guarda apenas o URI e a permissão que o usuário concedeu. O app não altera
  os arquivos originais e não mantém cópias deles; só em casos raros, quando o provedor de
  arquivos não permite leitura direta, cria uma cópia temporária no cache, apagada em seguida.
- **Uma interface para todos os formatos.** `PageSource` entrega uma página por vez, seja de um CBZ,
  CBR, CB7, CBT, PDF ou de imagens soltas, e o leitor não precisa saber qual é o formato.
- **Pouca memória.** Cada página é decodificada reduzida e só quando pedida, e `PageLoader` mantém
  algumas páginas num cache LRU limitado por bytes. Arquivos grandes nunca são carregados por inteiro.
- **Detecção pelo conteúdo.** O tipo do arquivo vem da assinatura dos primeiros bytes, não da extensão.
- **Tarefas fora das telas.** Importações e varreduras rodam numa coroutine própria, uma de cada vez,
  e continuam se o usuário trocar de aba.
- **Banco versionado.** O esquema do Room é exportado (`room.schemaLocation`) para acompanhar a evolução.

## Características técnicas

- Funciona 100% offline. O `AndroidManifest.xml` do app não declara permissões (nenhuma de internet
  nem de acesso amplo ao armazenamento).
- O identificador do pacote continua `com.mangareader.app` e o banco continua `mangareader.db`, por
  compatibilidade com instalações existentes, mesmo depois da mudança do nome para Madoe.

## Como compilar e testar

Requisitos: JDK 17 e o Android SDK com a plataforma 35 (o Android Studio instala e configura os dois).
O projeto traz o Gradle Wrapper, então não é preciso instalar o Gradle.

```bash
git clone https://github.com/theux-dev/madoe.git
cd madoe

./gradlew assembleDebug            # gera app/build/outputs/apk/debug/app-debug.apk
./gradlew testDebugUnitTest        # testes de unidade (JVM)
./gradlew connectedDebugAndroidTest  # testes instrumentados (exigem dispositivo ou emulador)
```

No Windows, use `gradlew.bat`. Se o Gradle não achar o SDK, crie `local.properties` com
`sdk.dir=/caminho/do/Android/Sdk` ou defina `ANDROID_HOME`.

Um workflow do GitHub Actions (`.github/workflows/build.yml`) roda os testes de unidade e a
compilação de debug a cada push. Os testes instrumentados não rodam lá, porque exigem um emulador.

## Limitações conhecidas

- Arquivos protegidos por senha não são suportados.
- Arquivos RAR e 7z "sólidos" muito grandes podem ser lentos ao pular para páginas distantes,
  porque o formato obriga a descompactar o que vem antes.
- O zoom por pinça existe só no modo página a página; ainda não há zoom por toque duplo nem no modo
  de rolagem contínua.
- Ainda não é possível compartilhar ou salvar uma página como imagem.
- Se um arquivo for movido ou apagado, o leitor avisa ao tentar abri-lo e oferece remover o título,
  mas os cartões da biblioteca ainda não são marcados como indisponíveis automaticamente.
- Os testes manuais foram feitos até agora principalmente em um aparelho Android.
- O projeto não está publicado em nenhuma loja de aplicativos.

## Copyright and Usage

### Português

© 2026 Matheus Barbosa (theux-dev). Todos os direitos reservados.

Este repositório é público para fins de apresentação e consulta (portfólio). O código-fonte e os
demais materiais, incluindo o ícone e os recursos gráficos do aplicativo, não são oferecidos sob
uma licença de uso geral: **não concedo autorização geral** para copiar, modificar, redistribuir ou
explorar comercialmente o código ou qualquer parte do projeto sem a minha permissão prévia.

O que continua valendo:

- **Funcionamento do GitHub.** Como o repositório é público, aplicam-se os Termos de Serviço do
  GitHub, que permitem aos usuários da plataforma ver o repositório e usar o conteúdo dentro do
  próprio GitHub, inclusive criando *forks* pelas funcionalidades dele. Essa permissão se limita ao
  funcionamento da plataforma e não autoriza usar, modificar, redistribuir ou explorar o código
  fora dela. Valem sempre os Termos de Serviço do GitHub em vigor.
- **Direitos previstos em lei.** Permanecem os usos que a legislação permite independentemente de
  autorização (limitações e exceções aos direitos autorais).
- **Componentes de terceiros.** Bibliotecas e a fonte têm licenças próprias, que se aplicam a elas e
  não são alteradas por este aviso. Veja [`THIRD_PARTY_NOTICES.md`](THIRD_PARTY_NOTICES.md).

Este aviso **não impede tecnicamente** que alguém clone, baixe ou faça fork do repositório e **não é
uma garantia absoluta de proteção jurídica**: ele declara a minha posição sobre os direitos do
projeto. Para pedir permissão de uso, abra uma *issue* ou entre em contato pelo meu perfil no GitHub.

### English

© 2026 Matheus Barbosa (theux-dev). All rights reserved.

This repository is public for presentation and reference purposes (portfolio). The source code and
the other materials, including the app icon and graphic assets, are not offered under a general-use
license: **I do not grant general permission** to copy, modify, redistribute or commercially exploit
the code or any part of the project without my prior permission.

What still applies:

- **How GitHub works.** Because the repository is public, GitHub's Terms of Service apply. They allow
  users of the platform to view the repository and use its content within GitHub itself, including
  creating forks through GitHub's features. That permission is limited to how the platform works and
  does not authorize using, modifying, redistributing or commercially exploiting the code outside
  of it. GitHub's Terms of Service in force at any time always apply.
- **Rights granted by law.** Uses that the applicable law allows regardless of authorization
  (copyright limitations and exceptions) remain unaffected.
- **Third-party components.** Libraries and the font have their own licenses, which apply to them and
  are not changed by this notice. See [`THIRD_PARTY_NOTICES.md`](THIRD_PARTY_NOTICES.md).

This notice **does not technically prevent** anyone from cloning, downloading or forking the
repository, and it **is not an absolute guarantee of legal protection**: it states my position on
the project's rights. To ask for permission, open an issue or contact me through my GitHub profile.

## Contribuições

Sugestões, ideias e *issues* com relatos de problemas são bem-vindas. **Por enquanto, não estou
aceitando contribuições de código** (pull requests).

## Créditos

- Desenvolvido por Matheus Barbosa, com auxílio de IA (Claude).
- A interface foi inspirada em referências de leitores de quadrinhos existentes, sem uso de arte,
  nome ou marca de terceiros. O ícone e o logotipo do Madoe são originais deste projeto.
- Fonte Bowlby One, de Vernon Adams (SIL Open Font License 1.1).
- Bibliotecas de código aberto listadas em [`THIRD_PARTY_NOTICES.md`](THIRD_PARTY_NOTICES.md).

Perfil: [github.com/theux-dev](https://github.com/theux-dev)
