<div align="center">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="https://raw.githubusercontent.com/manassesmartins/Gestor-de-Producao/main/docs/banner-dark.png">
    <img src="https://raw.githubusercontent.com/manassesmartins/Gestor-de-Producao/main/docs/banner-light.png" alt="Gestor de Produção" width="100%">
  </picture>

  <br><br>

  <h1>🧵 Gestor de Produção</h1>
  <p>
    <strong>ERP têxtil 100% offline para pequenas confecções e facções</strong>
  </p>

  <p>
    <img src="https://img.shields.io/badge/Kotlin-2.2.10-purple?logo=kotlin" alt="Kotlin">
    <img src="https://img.shields.io/badge/Jetpack%20Compose-Material%203-green?logo=jetpackcompose" alt="Compose">
    <img src="https://img.shields.io/badge/Room-2.7.0-orange?logo=sqlite" alt="Room">
    <img src="https://img.shields.io/badge/Min%20SDK-24-brightgreen" alt="Min SDK 24">
    <img src="https://img.shields.io/badge/Target%20SDK-36-blue" alt="Target SDK 36">
    <img src="https://img.shields.io/badge/API%20level-14+-red" alt="API 14+">
    <img src="https://img.shields.io/badge/status-estável-success" alt="Status">
  </p>

  <br>

  <!-- Quick links -->
  <a href="#-funcionalidades">Funcionalidades</a> •
  <a href="#-capturas-de-tela">Capturas</a> •
  <a href="#-tecnologias">Tecnologias</a> •
  <a href="#-arquitetura">Arquitetura</a> •
  <a href="#-modelo-de-dados">Dados</a> •
  <a href="#-como-compilar">Compilar</a> •
  <a href="#-atualização-automática">Atualização</a>

  <br><br>
</div>

---

## 📌 Sobre

O **Gestor de Produção** é um aplicativo Android completo para **gerenciamento têxtil** — ideal para pequenas confecções, facções e empreendedores do ramo de moda íntima, vestuário e acessórios.

**100% offline e local**, todos os dados ficam armazenados no próprio dispositivo em um banco SQLite criptografado. Sem nuvem, sem servidor, sem dependência de internet para funcionar.

---

## ✨ Funcionalidades

### 💰 Financeiro & Dashboard
| Funcionalidade | Descrição |
|---|---|
| **Saldo atual** | Receitas vs. despesas com indicador de variação percentual |
| **Fluxo de caixa** | Entradas (verde) e saídas (vermelho) por semana |
| **Margem líquida** | Percentual de lucro sobre o faturamento |
| **Ticket médio** | Valor médio por pedido |
| **Custo por peça** | Cálculo automático baseado na produção |
| **Gráfico semanal** | Barras de lucro por semana |
| **Fechamento mensal** | Bloqueio de meses encerrados para evitar alterações |
| **Histórico arquivado** | Visualização de meses já fechados |
| **Investimentos** | Controle de aportes com valor total e valor abatido |

### 📋 Pedidos & Produção
- **Agendamento de pedidos** por cliente, semana e área de produção
- **Status tracking**: Pendente, Em Andamento, Concluído, Atrasado
- **Filtro por data** com visualização por semana
- **Alertas de urgência** com contagem de pedidos em atraso e próximos ao vencimento
- **Cálculo automático de valor total** (quantidade × valor unitário)
- **Comanda principal em PDF** para impressão e envio
- **Auto-suggest** de clientes e modelos de peça já cadastrados
- **Áreas de produção**: Costura, Corte, Bordado, Embalagem, Revisão, Geral

### 🧮 Cálculo de Custo de Peças
- **Peças por KG**: Calcule rendimento (peças/kg) e custo unitário
- Informe: tipo de pano, peso (kg), valor do kg (R\$/KG), quantidade de peças cortadas
- **Calculadora embutida**: calculadora eletrônica completa com operações básicas

### 👥 Recursos & Cadastros
- **Clientes** — nome e telefone
- **Funcionários** — nome e função (ex: Costureira, Cortador, etc.)
- **Modelos de Peça** — tipos de produto fabricados
- **Categorias de Gasto** — classificação de despesas
- **Pagamentos de Funcionários** — registro semanal com valor

### 🎨 Personalização da Marca
- **Nome da marca** e categoria de produção
- **Upload de logotipo** da galeria (armazenado como base64 no banco)
- **Tema Dark/Light** alternável
- **Seletor de cor HSV** interativo (matiz, saturação e brilho)
- **Presets de cor**: Rosa, Azul, Verde, Ouro, Rubi
- **Escala de fonte**: Pequeno, Normal, Grande, Extra Grande (acessibilidade)
- Tema dinâmico aplicado em toda a interface com Material 3

### 🔄 Sincronização P2P (Web)
- Pareamento com a versão web via **código PIN de 6 dígitos**
- Espelhamento de dados entre Android e navegador via MQTT
- Configuração remota da marca sincronizada

### 📄 Relatórios PDF
- **Relatório operacional** completo: resumo financeiro, margem, peças produzidas
- **Comanda/fatura do cliente** com tabela de itens, quantidades e totais
- Compartilhamento via qualquer aplicativo (WhatsApp, Email, etc.)

### 💾 Backup & Manutenção
- **Exportar banco de dados** completo (arquivo `.db`)
- **Importar banco de dados** de arquivos `.db` existentes
- Migração automática entre versões do banco (9 migrações)
- Fechamento mensal com bloqueio de dados históricos

### ⬆️ Atualização Integrada
- **Verificador automático** de novas versões via GitHub
- Download com barra de progresso
- Instalação direta do APK baixado
- Configurável: owner, repositório, branch, caminho do APK

---

## 📸 Capturas de Tela

<div align="center">
  <table>
    <tr>
      <td align="center"><img src="docs/screenshots/dashboard.png" alt="Dashboard" width="200"><br><sub>Dashboard Financeiro</sub></td>
      <td align="center"><img src="docs/screenshots/orders.png" alt="Pedidos" width="200"><br><sub>Agendamento de Pedidos</sub></td>
      <td align="center"><img src="docs/screenshots/transactions.png" alt="Transações" width="200"><br><sub>Controle de Gastos</sub></td>
      <td align="center"><img src="docs/screenshots/settings.png" alt="Configurações" width="200"><br><sub>Personalização da Marca</sub></td>
    </tr>
  </table>
  <p><em>⚠️ Adicione as imagens na pasta <code>docs/screenshots/</code></em></p>
</div>

---

## 🛠 Tecnologias

<div align="center">

| Categoria | Tecnologia |
|---|---|
| **Linguagem** | [Kotlin](https://kotlinlang.org/) 2.2.10 |
| **UI** | [Jetpack Compose](https://developer.android.com/jetpack/compose) + [Material 3](https://m3.material.io/) |
| **Banco de Dados** | [Room](https://developer.android.com/training/data-storage/room) 2.7.0 (SQLite) |
| **Processamento de anotações** | [KSP](https://kotlinlang.org/docs/ksp-overview.html) |
| **Networking** | [Retrofit](https://square.github.io/retrofit/) + [OkHttp](https://square.github.io/okhttp/) |
| **Serialização JSON** | [Moshi](https://github.com/square/moshi) (Kotlin + codegen) |
| **Imagens** | [Coil](https://coil-kt.github.io/coil/) (Compose) |
| **Câmera** | [CameraX](https://developer.android.com/training/camerax) |
| **QR/Barcode** | [Code Scanner](https://developers.google.com/ml-kit/code-scanner) (Google Play Services) |
| **MQTT** | [Eclipse Paho](https://www.eclipse.org/paho/) 1.2.5 |
| **Autenticação** | [Google Play Services Auth](https://developers.google.com/android/guides/overview) |
| **Corrotinas** | [Kotlinx Coroutines](https://github.com/Kotlin/kotlinx.coroutines) |
| **Testes** | JUnit, [Robolectric](http://robolectric.org/), [Roborazzi](https://github.com/takahirom/roborazzi) |
| **Build** | [Gradle](https://gradle.org/) + [Kotlin DSL](https://docs.gradle.org/current/userguide/kotlin_dsl.html) |

</div>

---

## 🏗 Arquitetura

```
📦 MVVM (Model-View-ViewModel)
┃
┣━━ 📱 View (Compose Screens)
┃   ┣ DashboardScreen         ← Painel financeiro
┃   ┣ TransactionsScreen      ← Gastos
┃   ┣ NewTransactionScreen    ← Novo gasto
┃   ┣ OrdersScreen            ← Pedidos
┃   ┣ CalculationsScreen      ← Custo de peças
┃   ┣ EmployeesScreen         ← Recursos e cadastros
┃   ┣ SettingsScreen          ← Ajustes
┃   ┣ BusinessSetupScreen     ← Onboarding
┃   ┗ ReportsScreen           ← Relatórios
┃
┣━━ 🧠 ViewModel
┃   ┗ TransactionViewModel    ← Estado central (StateFlow)
┃
┣━━ 🗄 Repository
┃   ┗ TransactionRepository   ← Abstração de dados (12 DAOs)
┃
┗━━ 💾 Data Layer
    ┣ RoomDatabase            ← ms_modaintima_database (v10)
    ┣ 12 Entities              ← Tabelas SQLite
    ┣ 11 DAOs                  ← Operações de banco
    ┣ SessionManager           ← SharedPreferences
    ┗ DatabaseBackupManager    ← Export/Import .db
```

### Padrões e decisões técnicas

| Decisão | Detalhe |
|---|---|
| **Navegação** | Abas nativas com `NavigationRail` (tablet) e `BottomNavigation` (celular) |
| **Estado** | `StateFlow` + `collectAsStateWithLifecycle()` |
| **DI** | Manual via `ViewModelProvider.Factory` |
| **Responsividade** | `BoxWithConstraints` para adaptar layout à largura |
| **Banco offline** | 100% local, sem dependência de rede |
| **Migrações** | 9 migrações progressivas com `fallbackToDestructiveMigration()` |
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
product_models           → Modelos de peça
investments              → Investimentos
brand_config             → Configuração da marca (singleton)
closed_months            → Meses encerrados
```

### Migrações do Banco

| Versão | Alterações |
|---|---|
| 1→5 | Versões iniciais |
| 5→6 | Adicionado `businessArea` e `status` em `orders` |
| 6→7 | Criada tabela `investments` |
| 7→8 | Criadas tabelas `clients`, `employees`, `employee_payments`, `product_models` |
| 8→9 | Adicionados `isDarkMode` e `fontSizeScale` em `brand_config` |
| 9→10 | Criada tabela `closed_months` |

---

## 🔧 Como Compilar

### Pré-requisitos

- Android Studio Ladybug (2024.2.1+) ou superior
- JDK 17+
- Gradle 9.5.1 (gerado automaticamente pelo wrapper)

### Passos

```bash
# 1. Clone o repositório
git clone https://github.com/manassesmartins/Gestor-de-Producao.git

# 2. Gere a chave de assinatura (primeira vez apenas)
cd Gestor-de-Producao
keytool -genkey -v \
  -keystore debug.keystore \
  -alias androiddebugkey \
  -storepass android \
  -keypass android \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -dname "CN=Android Debug, O=Android, C=US"

# 3. Crie o arquivo base64 para builds reproduzíveis
base64 -w 64 debug.keystore > debug.keystore.base64

# 4. Compile o APK de debug
./gradlew assembleDebug

# 5. (Opcional) Commite o debug.keystore.base64
git add debug.keystore.base64
git commit -m "Add stable debug keystore for reproducible builds"
```

O APK gerado estará em `app/build/outputs/apk/debug/app-debug.apk`.

---

## ⬆️ Atualização Automática

O app possui um **mecanismo de atualização embutido** que verifica novas versões no GitHub:

1. Na tela de **Configurações**, clique em **"Verificar atualizações"**
2. O app consulta o `version.json` no repositório remoto
3. Se houver uma versão mais nova, exibe changelog e botão para baixar
4. O download é feito com barra de progresso
5. Após o download, a instalação é iniciada automaticamente

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
- **Importar**: Restaura dados de um arquivo `.db` existente
- **Localização**: `Settings > Manutenção de Dados (SQLite)`

> ⚠️ Ao importar, o banco atual é substituído pelo arquivo importado.

---

## 🚀 CI/CD

O projeto usa **GitHub Actions** para compilar automaticamente o APK a cada push na branch `main`:

1. Configura JDK 17 e Gradle
2. Decodifica a keystore estável (`debug.keystore.base64`)
3. Atualiza a versão no `version.json`
4. Compila o APK de debug
5. Commita o APK compilado e o `version.json` atualizado

Workflow: `.github/workflows/build-apk.yml`

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
│       │   ├── AppDatabase.kt
│       │   ├── TransactionRepository.kt
│       │   ├── DatabaseBackupManager.kt
│       │   ├── SessionManager.kt
│       │   ├── MqttSyncManager.kt
│       │   ├── LiveSyncManager.kt
│       │   └── *.kt          → 12 Entities + 11 DAOs
│       └── ui/
│           ├── AtelierApp.kt           → App raiz
│           ├── TransactionViewModel.kt → ViewModel central
│           ├── GitHubUpdater.kt        → Atualizador
│           ├── screens/                → 9 telas
│           ├── theme/                  → Tema dinâmico Material 3
│           └── utils/
│               ├── PdfGenerator.kt     → Relatórios PDF
│               └── LogoDecoder.kt      → Decodificação de logo
├── version.json             → Versão atual e changelog
├── metadata.json            → Metadados do app
├── build.gradle.kts         → Build raiz
└── settings.gradle.kts      → Configurações do projeto
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
