# data_engineering

Đây là dự án thực hành các vấn đề thực tế của BeeCost khi làm Data Engineering trên dữ liệu lớn của các trang web E-Commerce.

Ngoại trừ các hình ảnh thuộc về thương hiệu của BeeCost, bạn có thể thoải mái sử dụng hay chia sẻ tài nguyên tại project này.

BeeCost Team cũng rất mong các nhận xét và đóng góp từ các bạn.

## Các phần mềm cần thiết

Công cụ của project này chỉ dùng command line trên terminal của các hệ điều hành debian, ubuntu, mac, phần mềm java 8, ant và git. Có thể nói bài tập đầu tiên của bạn sẽ là cài đặt những phần mềm này, theo hướng dẫn ở bên dưới.

Chỉ những phần mềm này là đủ bạn nhé.

Note: Các bạn đã thạo các phần mềm giúp việc coding dễ dàng hơn như Eclipse, NetBean hay IntelliJ, hãy thoải mái tự cài đặt để dùng, tuy nhiên BeeCost Team chúng mình sẽ khó hỗ trợ config project này đối với các phần mềm đó. Xin hãy chú ý nhé.

### Hệ điều hành

Debian, Ubuntu (tối thiểu 16.04), MacOs.

Nếu bạn đang dùng Windows, không vấn đề. Hãy cài một máy ảo hệ điều hành Ubuntu 16.04. Hướng dẫn [tại đây](https://theholmesoffice.com/installing-ubuntu-in-vmware-player-on-windows/).

Hoặc, nếu bạn nào biết dùng cloud service, hãy tạo một máy ảo nhỏ trên google cloud service hay amazon chẳng hạn, nên chọn hệ điều hành Ubuntu bản 16.04 trở lên nhé. Ví dụ với Google Cloud, hướng dẫn [tại đây](https://linuxhint.com/ubuntu_server_google_cloud/), GCP có lợi thế là tặng bạn $300 tiền server.

Cấu hình tối thiểu của máy tính: 1 CPU, 512M Ram.

### Java 8 (Open JDK)

Với hệ điều hành Ubuntu, Debian: Cài đặt từ terminal
```
sudo apt-get update
sudo apt install openjdk-8-jdk
```

Nếu cài đặt chính xác, khi gõ lệnh "java -version", bạn sẽ thấy như sau:
```
java -version

# Output
# openjdk version "1.8.0_162"
# OpenJDK Runtime Environment (build 1.8.0_162-8u162-b12-1-b12)
# OpenJDK 64-Bit Server VM (build 25.162-b12, mixed mode)
```

Đối với MacOs: Cài đặt theo hướng dẫn [tại đây](https://installvirtual.com/install-openjdk-8-on-mac-using-brew-adoptopenjdk/)

### Software

#### Ant
Với hệ điều hành Ubuntu, Debian: Cài đặt từ terminal
```
sudo apt-get update
sudo apt-get install ant
```

Với MacOS, cài Ant theo hướng dẫn [tại đây](https://www.mkyong.com/ant/how-to-apache-ant-on-mac-os-x/)

#### Git
Với hệ điều hành Ubuntu, Debian: Cài đặt từ terminal
```
sudo apt-get update
sudo apt-get install git
```

Với MacOS: Cài Git theo hướng dẫn [tại đây](https://hackernoon.com/install-git-on-mac-a884f0c9d32c).

## Code download

Tốt rồi, nếu bạn đã làm đúng các bước. Thì hãy vào terminal, các lệnh sau sẽ download project này về máy của bạn:
```
Tungs-MacBook-Pro:workspace tung$ mkdir -p ~/workspace && cd ~/workspace
Tungs-MacBook-Pro:workspace tung$ git clone https://github.com/beecost/data_engineering && cd data_engineering
Cloning into 'data_engineering'...
remote: Enumerating objects: 6, done.
remote: Counting objects: 100% (6/6), done.
remote: Compressing objects: 100% (4/4), done.
remote: Total 6 (delta 1), reused 0 (delta 0), pack-reused 0
Receiving objects: 100% (6/6), done.
Resolving deltas: 100% (1/1), done.
Tungs-MacBook-Pro:data_engineering tung$
```

## Code build

Sau khi download code và vào thư mục code của project, hãy build bằng ant, như thế này là bạn đã thành công.
```
# hãy chắc là bạn có code version mới nhất
Tungs-MacBook-Pro:data_engineering tung$ git pull origin master
From github.com:beecost/data_engineering
 * branch            master     -> FETCH_HEAD
Already up to date.

# build bằng ant
Tungs-MacBook-Pro:data_engineering tung$ ant clean && ant
```


