package tunnel

import (
	"context"
	"sync"
	"time"

	"github.com/metacubex/mihomo/adapter/outboundgroup"
	C "github.com/metacubex/mihomo/constant"
	"github.com/metacubex/mihomo/constant/provider"
	"github.com/metacubex/mihomo/log"
	"github.com/metacubex/mihomo/tunnel"
)

const healthCheckTimeout = 5 * time.Second

func probeURL(proxy C.Proxy) {
	testURL := "https://www.gstatic.com/generate_204"
	for k := range proxy.ExtraDelayHistories() {
		if len(k) > 0 {
			testURL = k
			break
		}
	}

	ctx, cancel := context.WithTimeout(context.Background(), healthCheckTimeout)
	defer cancel()

	if _, _, err := proxy.URLTest(ctx, testURL, nil); err != nil && ctx.Err() == nil {
		log.Warnln("Request health check failed", err.Error())
	}
}

func HealthCheck(name string) {
	p := tunnel.Proxies()[name]

	if p == nil {
		log.Warnln("Request health check for `%s`: not found", name)

		return
	}

	g, ok := p.Adapter().(outboundgroup.ProxyGroup)
	if !ok {
		probeURL(p)

		return
	}

	wg := &sync.WaitGroup{}

	for _, pr := range g.Providers() {
		wg.Add(1)

		go func(provider provider.ProxyProvider) {
			provider.HealthCheck()

			wg.Done()
		}(pr)
	}

	wg.Wait()
}

func HealthCheckAll() {
	for _, g := range QueryProxyGroupNames(false) {
		go func(group string) {
			HealthCheck(group)
		}(g)
	}
}

func HealthCheckProxy(groupName string, proxyName string) {
	p := tunnel.Proxies()[groupName]

	if p == nil {
		log.Warnln(
			"Request health check for proxy `%s` in group `%s`: group not found",
			proxyName,
			groupName,
		)
		return
	}

	g, ok := p.Adapter().(outboundgroup.ProxyGroup)
	if !ok {
		log.Warnln(
			"Request health check for proxy `%s` in group `%s`: not a proxy group",
			proxyName,
			groupName,
		)
		return
	}

	for _, proxy := range g.Proxies() {
		if proxy.Name() == proxyName {
			probeURL(proxy)
			return
		}
	}

	log.Warnln(
		"Request health check for proxy `%s` in group `%s`: proxy not found",
		proxyName,
		groupName,
	)
}
