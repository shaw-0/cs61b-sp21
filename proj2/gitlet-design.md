# Gitlet Design Document

**Name**:

## Classes and Data Structures

### Main
程序入口点。从命令行接受参数，根据命令参数调用不同的函数。
调用的函数来自Repository类。

#### Fields
无


### Repository
中介。负责相应main的调用并调用其他的各种类各种方法。
负责程序主要逻辑。


#### Fields

1. static final File CWD，程序当前工作目录。
2. static final File GITLET_FOLDER，存储gitlet状态的目录。
3. static final File BLOBS_FOLDER，存储blobs的目录。
4. static final File ADD_STAGING，添加暂存目录。
5. static final File RM_STAGING，删除暂存目录。
6. static final File COMMIT_FOLDER，存储commit信息的目录。

### Commit

commit类包括：date、msg与当前版本文件列表与**blob对应的哈希值**，如“a.txt->asfihcwniekdim”。

使用ADT的map表示存储的文件，为了方便查找使用BST（暂定）。除了BST之外其他的删除太麻烦，BST最坏也就是个链表。
如果真有机会想攀登高峰的话建议一步到位红黑树。虽然是个tree，但是实现的是map哒！

每次工作（add等等）都会新建一个working_commit，操作会改变这个commit的内容。提交之后，commit被写入**再也不能改啦**的文件，文件名也改成相应的哈希值。

不存在指针，不会保存除了这次commit之外的冗余信息。

#### Fields

1. 

### CommitTree

用来描述提交情况的类，包括父指针、date、msg与对应commit的**哈希值**。

按照java序列化遵循指针的说法，保存了最新节点就相当于保存了整棵树，而真正的commit文件作为哈希字符串被储存。
存储大头在文件，但是其实commit存的也是哈希，好像也没方便到哪里去。

#### Fields

1. CommitTree parent，一般情况的parent。
2. CommitTree parent_merge，merge情况的第二个parent，最多就俩parent别搞数组了
3. String commit_ref，如同字面意思！就是对应commit的reference。

## Algorithms
数据结构编不出来了，我先编算法

### init
在当前目录中创建一个新的 Gitlet 版本控制系统。
此系统将自动启动一个提交：一个不包含任何文件且提交信息为 initial commit 的提交。
它将有一个分支： master ，它最初指向这个初始提交，而 master 将是当前分支。
此初始提交的时间戳为“The (Unix) Epoch”。

 - Runtime：O(1)

创建固定内容的commit。确实runtime是常数，不需要特别的算法。

1. 创建相应的文件夹结构
2. 创建固定内容的commit类，没有文件内容
3. 剩下的交给commit操作


### add
将当前存在的文件副本添加到暂存区。

1. 暂存区有了：覆盖旧条目。
2. 文件版本与提交版本一模一样：不加；暂存区有了（不同版本的）：移除
3. 之前删除过：删了还在目录里存在只有两种可能：(1) 之前add过但是取消了；(2) 没跟踪过。所以加就对了。
   1. 想得美。
   2. 之前删除过，不在目录里，但是在删除暂存区里：这个文件曾经被跟踪，但是删过一次，于是它就从跟踪目录与真的CWD中消失了，出现在了删除区里——那就把人家从删除暂存区里挪出来。
   3. 挪出来之后呢？恢复到目录里吗？没写就是不。恢复跟踪吗？没写就是不。
   
 - Runtime：O(N): N=文件大小；O(lg N): N=文件数量

1. add逻辑交给Repository类实现，具体操作看情况交给哪位勇士
2. 在CWD中搜索要add的文件（二分查找？）
3. 根据具体情况处理文件，比较文件是否一致就用哈希值罢……
4. 先看当前提交的版本是否一致（通过head指针指向的commit类判断，commit类会（以BST的方式）存储文件列表
5. 暂存区的不用看，要么删了要么覆盖。
6. 最后看一眼删除区有没有
7. failure case在最开始进行判断。

---
我觉得需要考虑一下该死的暂存区的问题。

如何确定一个文件是否被追踪？
——曾经被add到暂存区的都追踪。

如果一个文件在CWD中，但是没有被add过？
——不追。

如何确定文件追踪列表？
——交给commit，让它存一下自己这个版本追踪了什么文件。

暂存区里到底应该存什么？
——存文件

哈希值什么时候计算？
——提交的时候，暂存区里的文件使用正常的文件名称

提交的时候如何确定我该追踪的文件？
——

### commit
保存当前提交和暂存区中被跟踪文件的快照，创建一个新提交。 
提交将保存并开始跟踪任何已暂存以供添加但未被其父提交跟踪的文件。
最后，当前提交中被跟踪的文件可能会在新提交中被取消跟踪，这可能是由于通过 rm 命令暂存以供删除所导致的。

- 提交后，暂存区会被清空。
- 在暂存以供添加或删除后对文件所做的任何更改都将被提交命令忽略，提交命令只会修改 .gitlet 目录的内容。例如，如果您使用 Unix 的 rm 命令（而不是 Gitlet 的同名命令）删除一个被跟踪的文件，这对下一个提交没有影响，下一个提交仍将包含该文件的（现在已删除的）版本。
- 刚刚创建的提交成为“当前提交”，头指针现在指向它。之前的头提交是该提交的父提交。
- 每个提交通过其 SHA-1 id 进行标识，该 id 必须包括其文件的 blob 引用、父引用、日志消息和提交时间。
- Runtime：O(1)，N=提交数量；O(N)，N=跟踪的文件总大小。

1. 首先检查当前CWD下的文件，检查rm暂存区确定要跟踪的文件列表
2. 检查add暂存区，把更新的文件指向新的blob，其余指向父节点的blob
3. 把commit存储到相应文件夹
4. 把commit的ref存储起来，更新commit树(head)


### rm
1. add了：取消add。
2. 跟踪了：标记为删除，顺便把真文件也删了。
3. 没跟踪：不删。

- Runtime: O(1)

---删-除-应-该-用-不-到-暂-存-区-。---

想得美，需要。

在情况2的时候，把真文件删了，同时在删除区里存一下。

### log
沿着提交树直到初始提交，遵循第一个父提交链接，忽略在合并提交中发现的任何第二个父提交。

对于合并提交（具有两个父提交的提交），在第一个提交下方添加一行。

- Runtime: O(N)，N=历史中的节点数量。

### global-log
显示所有曾经做过的提交的信息。提交的顺序无关紧要。

- Runtime: O(N)，N=commit数量

目前为止最简单的一位，遍历commit文件夹就ok
……如果不需要显示merge的父节点的话。我先按照不需要理解，需要再改。

### find
打印出所有具有给定提交信息的提交 ID，每行一个。
如果有多个这样的提交，它会在不同的行上打印出这些 ID。

- Runtime: O(N)，N=提交数量。

### status
显示当前存在的分支。

未暂存的修改和未跟踪的文件是额外加分项。

条目应按字典顺序列出，使用 Java 字符串比较顺序（星号不计入）。

- Runtime: 仅依赖于工作目录中的数据量，加上待添加或删除的文件数量，加上分支的数量。


## Persistence

拟使用的文件结构。

```
CWD                             <==== current working directory
└── .gitlet                     <==== All persistant data is stored within here
    ├── head                    <==== 
    └── staging                 <==== staging area
    |   └── add
    |       ├── file1
    |       ├── file2
    |       ├── ...
    |       └── fileN
    └── commit                  <==== All commits are stored in this directory
    |   ├── commit1             <==== A commit file, whose name will be its hash code
    |   ├── commit2
    |   ├── ...
    |   └── commitN
    └── blob                    <==== All blobs are stored in this directory
        ├── blob1               <==== blob file, whose name will be its hash code
        ├── blob2
        ├── ...
        └── blobN
```
