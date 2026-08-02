<div align="center">

  <h1>🧵 Gestor de Produção</h1>
  <p>
    <strong>ERP têxtil para pequenas confecções e facções — Android + Web</strong>
  </p>

  <p>
    <img src="https://img.shields.io/badge/Kotlin-2.2.10-purple?logo=kotlin" alt="Kotlin">
    <img src="https://img.shields.io/badge/Jetpack%20Compose-Material%203-green?logo=jetpackcompose" alt="Compose">
    <img src="https://img.shields.io/badge/Room-2.7.0-orange?logo=sqlite" alt="Room">
    <img src="https://img.shields.io/badge/Min%20SDK-24-brightgreen" alt="Min SDK 24">
    <img src="https://img.shields.io/badge/Target%20SDK-36-blue" alt="Target SDK 36">
    <img src="https://img.shields.io/badge/status-estável-success" alt="Status">
  </p>

  <br>

  <!-- Quick links -->
  <a href="#-sobre">Sobre</a> •
  <a href="#-funcionalidades">Funcionalidades</a> •
  <a href="#-tecnologias">Tecnologias</a> •
  <a href="#-arquitetura">Arquitetura</a> •
  <a href="#-modelo-de-dados">Dados</a> •
  <a href="#-como-compilar">Compilar</a> •
  <a href="#-atualização-automática">Atualização</a>

  <br><br>
</div>

---

## 📌 Sobre

O **Gestor de Produção** é um sistema completo de gerenciamento para o ramo têxtil — ideal para pequenas confecções, facções e empreendedores de moda íntima, vestuário e acessórios.

O projeto é composto por **dois aplicativos que compartilham o mesmo modelo de dados**:

- 📱 **App Android** (Kotlin + Jetpack Compose) — **100% offline e local**, com todos os dados armazenados no próprio dispositivo em um banco SQLite (Room). Funciona sem internet.
- 🌐 **Versão Web** (SPA em HTML/JS puro) — espelha o mesmo sistema no navegador, com suporte a login, sincronização em nuvem (Supabase) e pareamento com o app Android via MQTT.

---

## ✨ Funcionalidades

### 💰 Financeiro & Dashboard
| Funcionalidade | Descrição |
|---|---|
| **Saldos do mês** | "Saldo do Mês", "Anterior + Atual" (padrão) e variação percentual |
| **Fluxo de caixa** | Entradas (verde) e saídas (vermelho) por semana |
| **Margem líquida** | Percentual de lucro sobre o faturamento |
| **Ticket médio** | Valor médio por pedido |
| **Custo por peça** | Cálculo automático baseado na produção |
| **Gráfico semanal** | Barras de lucro por semana |
| **Fechamento mensal** | Bloqueio de meses encerrados para evitar alterações (DRE na web) |
| **Histórico arquivado** | Visualização de meses já fechados |
| **Investimentos** | Controle de aportes com valor total e valor abatido |
| **Regra de fim de mês** | Gastos da última semana do mês contam na 1ª semana do mês seguinte |
| **Relatório do mês** | Saldo do mês anterior, saldo atual e saldo total consolidado |

### 📋 Pedidos & Produção
- **Agendamento de pedidos** por cliente, semana e área de produção
- **Status tracking**: Pendente, Em Andamento, Concluído, Atrasado
- **Filtro por data** com visualização por semana
- **Alertas de urgência** com contagem de pedidos em atraso e próximos ao vencimento
- **Cálculo automático de valor total** (quantidade × valor unitário)
- **Comanda do pedido** compartilhável em **PDF** ou **imagem (JPG)**
- **Auto-suggest** de clientes e modelos de peça já cadastrados
- **Áreas de produção**: Costura, Corte, Bordado, Embalagem, Revisão, Geral
- **Modelos com preço por tamanho** (P, M, G, GG, U)

### 🧮 Cálculo de Custo de Peças
- **Peças por KG**: rendimento (peças/kg) e custo unitário
- Informe: tipo de pano, peso (kg), valor do kg (R\$​/KG) e quantidade cortada
- **Calculadora embutida**: calculadora eletrônica completa com operações básicas

### 👥 Recursos & Cadastros
- **Clientes** — nome e telefone
- **Funcionários** — nome e função (ex: Costureira, Cortador)
- **Modelos de Peça** — tipos de produto com preço e preço por tamanho
- **Categorias de Gasto** — classificação com histórico de descrições (renomear/excluir)
- **Pagamentos de Funcionários** — registro semanal com valor e status
- **Gerenciar Expediente Local** — gerenciamento de sessão pelo perfil

### 🎨 Personalização da Marca
- **Nome da marca** e categoria de produção
- **Upload de logotipo** da galeria (armazenado como base64 no banco)
- **Tema Dark/Light** alternável
- **Seletor de cor HSV** interativo (matiz, saturação e brilho)
- **Presets de cor**: Rosa, Azul, Verde, Ouro, Rubi
- **Escala de fonte**: Pequeno, Normal, Grande, Extra Grande (acessibilidade)
- Tema dinâmico aplicado em toda a interface com Material 3

### 📄 Relatórios PDF
- **Relatório operacional** completo: resumo financeiro, margem, peças produzidas
- **Relatório do mês** com saldo do mês anterior, atual e total
- **Comanda/fatura do cliente** com tabela de itens, quantidades e totais
- Compartilhamento via qualquer aplicativo (WhatsApp, Email, etc.)

### 🌐 Versão Web
- SPA **offline-first** com dados locais no navegador (localStorage)
- **Login e nuvem Supabase** opcionais (configuráveis por URL/API key)
- **Pareamento com o Android** via **código PIN de 6 dígitos** ou **QR Code**
- **Importar banco de dados** (.db exportado do app Android) via `sql.js`
- Painel com sub-abas: Painel, Apurado Semanal, Fechamento Mensal (DRE)
- **Impressão/PDF** dos relatórios pelo navegador

### 🔄 Sincronização P2P (Android ↔ Web)
- Pareamento por **PIN de 6 dígitos** e **QR Code**
- Espelhamento de dados em tempo real via **MQTT (HiveMQ Cloud, TLS)**
- Tópico dedicado `gestor_producao/sync/<PIN>` com QoS 1
- Sincronização de transações, pedidos, cálculos, gastos e configuração da marca
- Reconexão automática ao restabelecer a internet (auto-sync)

### 💾 Backup & Manutenção
- **Exportar banco de dados** completo (arquivo `.db`)
- **Importar banco de dados** de arquivos `.db` existentes (Android e web)
- Migração automática entre versões do banco (7 migrações, v5 → v12)
- Fechamento mensal com bloqueio de dados históricos

### ⬆️ Atualização Integrada
- **Verificação silenciosa** de novas versões ao abrir o app
- **Verificador manual** na tela de Configurações via GitHub
- Download com barra de progresso e instalação direta do APK
- Configurável: owner, repositório, branch, caminho do APK

---

## 🛠 Tecnologias

<div align="center">

| Categoria | Tecnologia |
|---|---|
| **Linguagem** | [Kotlin](https://kotlinlang.org/) 2.2.10 |
| **UI** | [Jetpack Compose](https://developer.android.com/jetpack/compose) + [Material 3](https://m3.material.io/) (Compose BOM 2024.09.00) |
| **Banco de Dados** | [Room](https://developer.android.com/training/data-storage/room) 2.7.0 (SQLite) |
| **Processamento de anotações** | [KSP](https://kotlinlang.org/docs/ksp-overview.html) 2.3.5 |
| **Networking** | [OkHttp](https://square.github.io/okhttp/) 4.10.0 |
| **MQTT** | [Eclipse Paho](https://www.eclipse.org/paho/) 1.2.5 + HiveMQ Cloud (TLS) |
| **Imagens** | [Coil](https://coil-kt.github.io/coil/) 2.7.0 (Compose) |
| **Corrotinas** | [Kotlinx Coroutines](https://github.com/Kotlin/kotlinx.coroutines) 1.10.2 |
| **Testes** | JUnit, [Robolectric](http://robolectric.org/) 4.16.1, [Roborazzi](https://github.com/takahirom/roborazzi) 1.59.0 |
| **Build** | AGP 9.1.1 + [Kotlin DSL](https://docs.gradle.org/current/userguide/kotlin_dsl.html) + Gradle 9.x |
| **Web** | HTML/JS puro + Tailwind (CDN) + `sql.js` + [Paho JS](https://www.eclipse.org/paho/) + Supabase JS |
| **CI/CD** | GitHub Actions (compila e publica o APK a cada push) |

</div>

---

## 🏗 Arquitetura

```
📦 MVVM (Model-View-ViewModel)
┃
┣━━ 📱 View (Compose Screens)
┃   ┣ DashboardScreen         ← Painel financeiro (saldos, gráficos)
┃   ┣ TransactionsScreen      ← Gastos / transações
┃   ┣ NewTransactionScreen    ← Novo gasto
┃   ┣ OrdersScreen            ← Pedidos
┃   ┣ CalculationsScreen      ← Custo de peças
┃   ┣ EmployeesScreen         ← Recursos e cadastros
┃   ┣ SettingsScreen          ← Ajustes e sincronização
┃   ┣ BusinessSetupScreen     ← Onboarding / setup da marca
┃   ┗ ProfileSettingsPopup    ← Gerenciar expediente local
┃
┣━━ 🧠 ViewModel
┃   ┗ TransactionViewModel    ← Estado central (StateFlow)
┃
┣━━ 🗄 Repository
┃   ┗ TransactionRepository   ← Abstração de dados (12 DAOs)
┃
┗━━ 💾 Data Layer
    ┣ RoomDatabase            ← ms_modaintima_database (v12)
    ┣ 12 Entities             ← Tabelas SQLite
    ┣ 12 DAOs                 ← Operações de banco
    ┣ LiveSyncManager         ← Sincronização MQTT (P2P)
    ┣ SessionManager          ← SharedPreferences
    ┗ DatabaseBackupManager   ← Export/Import .db
```

### Padrões e decisões técnicas

| Decisão | Detalhe |
|---|---|
| **Navegação** | Abas nativas com `NavigationRail` (tablet) e `BottomNavigation` (celular) |
| **Estado** | `StateFlow` + `collectAsStateWithLifecycle()` |
| **DI** | Manual via `ViewModelProvider.Factory` |
| **Responsividade** | `BoxWithConstraints` para adaptar layout à largura |
| **Banco offline** | 100% local (Room), sem dependência de rede |
| **Migrações** | 7 migrações progressivas com `fallbackToDestructiveMigration()` |
| **Versão** | `versionCode` calculado automaticamente a partir do `version.json` |
| **Assinatura** | `debug.keystore.base64` commitado para builds reproduzíveis |

---

## 📊 Modelo de Dados

### Entidades (12 tabelas)

```
users                    → Usuários (autenticação local)
transactions             → Movimentações financeiras (entrada/saída)
categories               → Categorias de gasto
orders                   → Pedidos de produção
piece_calculations       → Cálculo de custo de peças
clients                  → Clientes
employees                → Funcionários
employee_payments        → Pagamentos de funcionários
product_models           → Modelos de peça (preço + preço por tamanho)
investments              → Investimentos
brand_config             → Configuração da marca (singleton)
closed_months            → Meses encerrados
```

### Migrações do Banco

| Versão | Alterações |
|---|---|
| 5→6 | Adicionados `businessArea` e `status` em `orders` |
| 6→7 | Criada tabela `investments` |
| 7→8 | Criadas tabelas `clients`, `employees`, `employee_payments`, `product_models` |
| 8→9 | Adicionados `isDarkMode` e `fontSizeScale` em `brand_config` |
| 9→10 | Criada tabela `closed_months` |
| 10→11 | Adicionada coluna `price` em `product_models` |
| 11→12 | Adicionada coluna `sizePrices` (preço por tamanho) em `product_models` |

---

## 🔧 Como Compilar

### Pré-requisitos

- Android Studio Ladybug (2024.2.1+) ou superior
- JDK 17+
- Gradle 9.x (AGP 9.1.1). O CI gera o wrapper com `gradle wrapper --gradle-version 9.5.1`

### Passos

```bash
# 1. Clone o repositório
git clone https://github.com/manassesmartins/Gestor-de-Producao.git
cd Gestor-de-Producao

# 2. A chave de assinatura é restaurada automaticamente do debug.keystore.base64
#    (se não existir, a tarefa generateDebugKeystore gera uma nova)

# 3. Compile o APK de debug
./gradlew assembleDebug
```

O APK gerado estará em `app/build/outputs/apk/debug/app-debug.apk`. O mesmo APK também fica disponível na raiz do repositório (`app-debug.apk`) para o atualizador automático.

### Versão Web

A versão web (`web/index.html`) é gerada/aplicada pelos scripts `patchHtml.gradle.kts` e `patchWebQr.gradle.kts` (pareamento por QR Code) e é publicada como site estático (Vercel, `vercel.json` → `outputDirectory: web`).

---

## ⬆️ Atualização Automática

O app possui um **mecanismo de atualização embutido** que verifica novas versões no GitHub:

1. Ao abrir o app, uma **verificação silenciosa** consulta a última versão
2. Na tela de **Configurações**, o botão **"Verificar atualizações"** força a verificação
3. O app consulta o `version.json` no repositório remoto
4. Se houver uma versão mais nova, exibe changelog e botão para baixar
5. O download é feito com barra de progresso
6. Após o download, a instalação é iniciada automaticamente

### Configuração padrão

| Parâmetro | Valor |
|---|---|
| **Owner** | `manassesmartins` |
| **Repo** | `Gestor-de-Producao` |
| **Branch** | `main` |
| **APK Path** | `app-debug.apk` |
| **Version JSON** | `version.json` |

---

## 🔐 Backup de Dados

O app oferece exportação e importação completa do banco SQLite:

- **Exportar**: Gera um arquivo `.db` com todos os dados
- **Importar**: Restaura dados de um arquivo `.db` existente (no Android e na versão web)
- **Localização**: `Settings > Manutenção de Dados (SQLite)`

> ⚠️ Ao importar, o banco atual é substituído pelo arquivo importado.

---

## 🚀 CI/CD

O projeto usa **GitHub Actions** (`.github/workflows/build-apk.yml`) para compilar automaticamente o APK a cada push na branch `main`:

1. Configura JDK 17 e Gradle (gera o wrapper com Gradle 9.5.1)
2. Decodifica a keystore estável (`debug.keystore.base64`)
3. Atualiza a versão no `version.json` a partir do número do run
4. Compila o APK de debug
5. Commita o APK compilado e o `version.json` atualizado

---

## 📁 Estrutura do Projeto

```
Gestor-de-Producao/
├── .github/workflows/       → CI/CD (build-apk.yml)
├── app/
│   ├── build.gradle.kts     → Build config
│   └── src/main/java/com/example/
│       ├── MainActivity.kt
│       ├── data/
│       │   ├── AppDatabase.kt          → Banco v12 (12 entidades)
│       │   ├── TransactionRepository.kt
│       │   ├── DatabaseBackupManager.kt
│       │   ├── SessionManager.kt
│       │   ├── LiveSyncManager.kt      → Sincronização MQTT
│       │   └── *.kt                    → 12 Entities + 12 DAOs
│       └── ui/
│           ├── AtelierApp.kt           → App raiz (nav por abas)
│           ├── TransactionViewModel.kt → ViewModel central
│           ├── GitHubUpdater.kt        → Atualizador
│           ├── screens/                → 8 telas + popups
│           ├── theme/                  → Tema dinâmico Material 3
│           └── utils/
│               ├── PdfGenerator.kt     → Relatórios PDF / comanda (JPG)
│               └── LogoDecoder.kt      → Decodificação de logo
├── web/index.html          → Versão web (SPA)
├── patchHtml.gradle.kts    → Geração/patch do web/index.html
├── patchWebQr.gradle.kts   → Pareamento QR Code na web
├── vercel.json             → Deploy estático (outputDirectory: web)
├── version.json            → Versão atual e changelog
├── metadata.json           → Metadados do app
├── debug.keystore.base64   → Chave de assinatura estável
├── app-debug.apk           → Último APK compilado (pelo CI)
├── build.gradle.kts        → Build raiz
└── settings.gradle.kts     → Configurações do projeto
```

---

## 📄 Licença

```
Copyright (c) 2025 Manassés Martins

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
```

---

<div align="center">
  <p>
    Feito com 💙 por <a href="https://github.com/manassesmartins">Manassés Martins</a>
  </p>
  <p>
    <a href="https://github.com/manassesmartins/Gestor-de-Producao/issues">Reportar Bug</a> •
    <a href="https://github.com/manassesmartins/Gestor-de-Producao/discussions">Discussões</a> •
    <a href="https://github.com/manassesmartins/Gestor-de-Producao/releases">Releases</a>
  </p>
</div>
