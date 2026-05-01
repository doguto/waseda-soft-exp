# パッケージマネージャー Scoop のインストール
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
Invoke-RestMethod -Uri https://get.scoop.sh | Invoke-Expression
scoop --version

# ビルドツール Maven のインストール
scoop isntall main/maven
mvn -version
