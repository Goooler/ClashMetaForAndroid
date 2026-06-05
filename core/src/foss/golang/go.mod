module foss

go 1.20

require cfa v0.0.0

require (
	github.com/dlclark/regexp2 v1.12.0 // indirect
	golang.org/x/sync v0.11.0 // indirect
)

replace cfa => ../../main/golang

replace github.com/metacubex/mihomo => ./clash

replace google.golang.org/protobuf => github.com/metacubex/protobuf-go v0.0.0-20260306035419-7ceee0674686
