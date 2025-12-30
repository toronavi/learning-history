# Android ライブラリ一覧

Android開発でよく利用される、デファクトスタンダードなライブラリをカテゴリ別にまとめます。
詳細は各ライブラリのリンクから参照してください。

## DI (Dependency Injection)

-   **[Hilt](detail/hilt.md)**
    -   [公式ドキュメント](https://developer.android.com/training/dependency-injection/hilt-android)
    -   Daggerをベースにした、よりシンプルで使いやすいDIライブラリ。Jetpack推奨。
-   **[Dagger 2](detail/dagger2.md)**
    -   [公式ドキュメント](https://dagger.dev/)
    -   コンパイル時DIのデファクトスタンダード。パフォーマンスが高い。
-   **[Koin](https://insert-koin.io/)**
    -   Kotlinで書かれた軽量なDIフレームワーク。学習コストが低い。

## 通信

-   **[Retrofit](https://square.github.io/retrofit/)**
    -   HTTPクライアントライブラリ。OkHttpと組み合わせて使うことが一般的。
-   **[OkHttp](https://square.github.io/okhttp/)**
    -   Square製の強力なHTTPクライアント。
-   **[Ktor Client](https://ktor.io/docs/client-overview.html)**
    -   JetBrains製のKotlin Multiplatform対応のHTTPクライアント。

## 画像読み込み

-   **[Glide](https://github.com/bumptech/glide)**
    -   パフォーマンスが良く、多機能な画像読み込みライブラリ。
-   **[Coil](https://coil-kt.github.io/coil/)**
    -   Kotlin Coroutinesベースのモダンな画像読み込みライブラリ。

## 非同期処理

-   **[Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)**
    -   Kotlinに組み込まれた非同期処理の仕組み。Jetpackでも広く使われている。
-   **[RxJava / RxKotlin / RxAndroid](https://github.com/ReactiveX/RxJava)**
    -   リアクティブプログラミングを実現するためのライブラリ。

## データベース

-   **[Room](https://developer.android.com/training/data-storage/room)**
    -   SQLiteのORM。Jetpackの一部で、公式に推奨されている。
-   **[Realm](https://realm.io/)**
    -   モバイル向けに設計された高速なデータベース。

## ナビゲーション

-   **[Navigation Component](https://developer.android.com/guide/navigation/navigation-getting-started)**
    -   画面遷移を管理するためのJetpackライブラリ。

## ビューとUI

-   **[Jetpack Compose](https://developer.android.com/jetpack/compose)**
    -   宣言的なUIを構築するためのモダンなUIツールキット。
-   **[Lottie](https://airbnb.io/lottie/)**
    -   Adobe After Effectsで作成したアニメーションを再生するためのライブラリ。
-   **[Material Components for Android](https://material.io/develop/android)**
    -   Material Designを実装するためのコンポーネント集。

## テスト

-   **[JUnit 4 / JUnit 5](https://junit.org/junit5/)**
    -   Java / Kotlinの単体テストフレームワーク。
-   **[Espresso](https://developer.android.com/training/testing/espresso)**
    -   UIテストを記述するためのフレームワーク。
-   **[MockK](https://mockk.io/)**
    -   Kotlin向けのモックライブラリ。
-   **[Robolectric](http://robolectric.org/)**
    -   実機やエミュレータなしでAndroidのテストを実行するためのフレームワーク。
