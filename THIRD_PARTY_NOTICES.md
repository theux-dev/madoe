# Avisos de terceiros / Third-party notices

O Madoe usa componentes de terceiros, cada um licenciado por seus autores nos termos próprios.
Essas licenças continuam valendo para esses componentes, e as obrigações delas se aplicam
independentemente do aviso de direitos autorais do código do Madoe (veja a seção
*Copyright and Usage* no [README](README.md)). A ausência de uma licença própria para o Madoe
**não** elimina nem altera as obrigações relacionadas às dependências.

*Madoe uses third-party components, each licensed by its authors under its own terms. Those
licenses remain valid for those components and apply regardless of the copyright notice for
Madoe's own code (see the "Copyright and Usage" section of the [README](README.md)).*

As versões abaixo são as declaradas em [`gradle/libs.versions.toml`](gradle/libs.versions.toml).
Dependências transitivas têm versões resolvidas pelo Gradle e podem variar.

## Incluídos no aplicativo

| Componente | Versão | Licença | Origem |
|---|---|---|---|
| AndroidX: Core KTX, Lifecycle, Activity Compose, Navigation Compose, Room, DataStore, Hilt Navigation Compose | 1.15.0, 2.8.7, 1.9.3, 2.8.5, 2.6.1, 1.1.1, 1.2.0 (nessa ordem) | Apache-2.0 | <https://developer.android.com/jetpack/androidx> |
| Jetpack Compose (UI, Material 3, Material Icons Extended) | BOM 2024.12.01 | Apache-2.0 | <https://developer.android.com/jetpack/compose> |
| Dagger / Hilt | 2.52 | Apache-2.0 | <https://github.com/google/dagger> |
| Coil | 2.7.0 | Apache-2.0 | <https://github.com/coil-kt/coil> |
| Okio (transitiva do Coil) | resolvida pelo Gradle | Apache-2.0 | <https://github.com/square/okio> |
| Apache Commons Compress | 1.27.1 | Apache-2.0 | <https://github.com/apache/commons-compress> |
| Apache Commons IO e Commons Lang (transitivas do Commons Compress) | resolvidas pelo Gradle | Apache-2.0 | <https://commons.apache.org> |
| junrar | 8.1.1 | Licença do UnRAR (veja abaixo) | <https://github.com/junrar/junrar> |
| SLF4J API (transitiva do junrar) | 2.0.17 | MIT (texto abaixo) | <https://github.com/qos-ch/slf4j> |
| XZ for Java | 1.10 | 0BSD (texto abaixo) | <https://github.com/tukaani-project/xz-java> |
| Kotlin (biblioteca padrão) | 2.0.21 | Apache-2.0 | <https://github.com/JetBrains/kotlin> |
| kotlinx.coroutines | resolvida pelo Gradle | Apache-2.0 | <https://github.com/Kotlin/kotlinx.coroutines> |

O texto da licença Apache-2.0 está em [`licenses/Apache-2.0.txt`](licenses/Apache-2.0.txt).

### Fonte

**Bowlby One**, copyright (c) 2011 Vernon Adams, licenciada sob a **SIL Open Font License 1.1**.
Arquivo no projeto: `app/src/main/res/font/bowlby_one.ttf` (usada sem modificações).
Texto oficial da licença, com o aviso de copyright e os nomes reservados:
[`licenses/OFL-BowlbyOne.txt`](licenses/OFL-BowlbyOne.txt). Origem: <https://github.com/google/fonts>
(pasta `ofl/bowlbyone`).

### junrar e a licença do UnRAR

O junrar lê arquivos RAR (CBR) e é distribuído com a licença do UnRAR, que é "freeware". Em resumo
(o texto completo e oficial está em [`licenses/UnRAR-junrar.txt`](licenses/UnRAR-junrar.txt),
e é ele que vale):

- Os direitos sobre o RAR e o UnRAR pertencem ao autor, Alexander Roshal.
- O código pode ser usado em qualquer software para **ler** arquivos RAR, sem custo, mas **não pode
  ser usado para recriar o algoritmo de compressão RAR**, que é proprietário.
- A distribuição de código modificado do UnRAR exige deixar claro, na documentação e nos comentários,
  que ele não pode ser usado para desenvolver um compactador compatível com RAR.

O Madoe só **lê** arquivos RAR e usa o junrar sem modificações. Ele não cria nem compacta arquivos RAR.

### SLF4J (MIT)

```
Copyright (c) 2004-2022 QOS.ch Sarl (Switzerland)
All rights reserved.

Permission is hereby granted, free  of charge, to any person obtaining
a  copy  of this  software  and  associated  documentation files  (the
"Software"), to  deal in  the Software without  restriction, including
without limitation  the rights to  use, copy, modify,  merge, publish,
distribute,  sublicense, and/or sell  copies of  the Software,  and to
permit persons to whom the Software  is furnished to do so, subject to
the following conditions:

The  above  copyright  notice  and  this permission  notice  shall  be
included in all copies or substantial portions of the Software.

THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
```

### XZ for Java (0BSD)

```
Copyright (C) The XZ for Java authors and contributors

Permission to use, copy, modify, and/or distribute this
software for any purpose with or without fee is hereby granted.

THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL
WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL
THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR
CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM
LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT,
NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN
CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
```

## Somente desenvolvimento e testes (não fazem parte do aplicativo)

| Componente | Versão | Licença |
|---|---|---|
| JUnit 4 | 4.13.2 | Eclipse Public License 1.0 (<https://github.com/junit-team/junit4>) |
| AndroidX Test (ext-junit, runner) | 1.2.1, 1.6.2 | Apache-2.0 |
| kotlinx-coroutines-test | 1.9.0 | Apache-2.0 |
| Gradle Wrapper (`gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`) | 8.9 | Apache-2.0 (arquivos oficiais do projeto Gradle, <https://github.com/gradle/gradle>) |

O Android Gradle Plugin e o KSP são usados só para compilar e não são distribuídos com o aplicativo.
